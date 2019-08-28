import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Disseminator {
    private final int maxGossipPerMessage;
    private MemberList memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer(new ConcurrentHashMap<>());
    private final Map<Member, Lock> mutexes = new ConcurrentHashMap<>();
    private final Map<Member, ScheduledFuture<?>> suspectTimers = new ConcurrentHashMap<>();
    private final Logger logger = LogManager.getLogger();
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    Disseminator(MemberList memberList, Config conf) {
        this.memberList = memberList;
        maxGossipPerMessage = conf.getMaxGossipPerMessage();
    }

    List<Gossip> generateMemberList() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member member : memberList.getList()) {
            gossipList.add(new Gossip(GossipType.ALIVE, member, member.getIncarnationNumber()));
        }
        return gossipList;
    }

    void mergeMemberList(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            memberList.add(gossip.getMember());
        }
    }

    List<Gossip> generateGossip() {
        return gossipBuffer.getItems(maxGossipPerMessage);
    }

    void mergeGossip(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            logger.debug("Merging gossip: {}", gossip);

            Lock mutex = getMutex(gossip.getMember());
            mutex.lock();
            try {
                if (gossip.getMember().equals(SwimJava.getSelf()) && gossip.getGossipType() == GossipType.SUSPECT) {
                    alive();
                    continue;
                }
                Member member = memberList.get(gossip.getMember());

                if (member == null) {
                    if (gossip.getGossipType() != GossipType.JOIN) return;
                    member = gossip.getMember();
                }

                boolean wasMerged = gossipBuffer.mergeItem(gossip);
                if (wasMerged) {
                    updateMemberState(member, gossip);
                }
            } finally {
                mutex.unlock();
            }
        }
    }

    void updateMemberState(Member member, Gossip gossip) {
        if (gossip.getIncarnationNumber() > member.getIncarnationNumber()) {
            member.setIncarnationNumber(gossip.getIncarnationNumber());
        }

        switch (gossip.getGossipType()) {
            case ALIVE:
                member.alive();
                cancelSuspectTimer(member);
                break;
            case SUSPECT:
                member.suspect();
                startSuspectTimer(member);
                break;
            case CONFIRM:
                memberList.remove(member);
                break;
            case JOIN:
                memberList.add(member);
                break;
        }
    }

    void startSuspectTimer(Member m) {
        ScheduledFuture<?> future = executorService.schedule(() -> {
            System.out.println("FIRE!!!");
            confirm(m);
        }, 5000, TimeUnit.MILLISECONDS);
        suspectTimers.put(m, future);
    }

    void cancelSuspectTimer(Member m) {
        ScheduledFuture<?> future = suspectTimers.get(m);
        if (future != null) {
            future.cancel(false);
        }
    }

    void alive() {
        logger.info("Marking {} as ALIVE", SwimJava.getSelf());
        Gossip alive = new Gossip(GossipType.ALIVE,
                SwimJava.getSelf(),
                SwimJava.getSelf().incrementAndGetIncarnationNumber());
        mergeGossip(Collections.singletonList(alive));
    }

    void suspect(Member m) {
        logger.info("Marking {} as SUSPECTED", m);
        Gossip suspect = new Gossip(GossipType.SUSPECT, m, m.getIncarnationNumber());
        mergeGossip(Collections.singletonList(suspect));
    }

    void confirm(Member m) {
        logger.info("Marking {} as DEAD", m);
        Gossip confirm = new Gossip(GossipType.CONFIRM, m, m.getIncarnationNumber());
        mergeGossip(Collections.singletonList(confirm));
    }

    private Lock getMutex(Member m) {
        Lock newMutexIfAbsent = new ReentrantLock();
        Lock existingMutex = mutexes.putIfAbsent(m, newMutexIfAbsent);
        return (existingMutex == null) ? newMutexIfAbsent : existingMutex;
    }

}
