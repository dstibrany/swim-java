import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Disseminator {
    private final int maxGossipPerMessage;
    private final Map<Member, Lock> mutexes = new ConcurrentHashMap<>();
    private final Map<Member, ScheduledFuture<?>> suspectTimers = new ConcurrentHashMap<>();
    private final Logger logger = LogManager.getLogger();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private final Member self;
    private final int suspicionTimeout;
    private MemberList memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer(new ConcurrentHashMap<>());

    Disseminator(MemberList memberList, Config conf) {
        this.memberList = memberList;
        maxGossipPerMessage = conf.getMaxGossipPerMessage();
        self = conf.getSelf();
        suspicionTimeout = conf.getSuspicionTimeout();
    }

    List<Gossip> generateMemberList() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member member : memberList.getAsList()) {
            gossipList.add(new Gossip(GossipType.ALIVE, member));
        }
        return gossipList;
    }

    void mergeMemberList(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            memberList.add(gossip.getMember());
        }
    }

    List<Gossip> generateGossip() {
        return gossipBuffer.getItems(maxGossipPerMessage, memberList.size());
    }

    void mergeGossip(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            logger.debug("Merging gossip: {}", gossip);

            Lock mutex = getMutex(gossip.getMember());
            mutex.lock();
            try {
                if (!memberList.contains(gossip.getMember()) && gossip.getGossipType() != GossipType.JOIN) {
                    continue;
                }

                if (gossip.getMember().equals(self) && gossip.getGossipType() == GossipType.SUSPECT) {
                    alive(self);
                    continue;
                }

                mergeItem(gossip);
            } finally {
                mutex.unlock();
            }
        }
    }

    // TODO: more accurate name
    void suspect(Member m) {
        Lock mutex = getMutex(m);
        mutex.lock();
        try {
            Gossip suspect = new Gossip(GossipType.SUSPECT, m);
            mergeItem(suspect);
        } finally {
            mutex.unlock();
        }
    }

    // TODO: more accurate name
    private void alive(Member m) {
        m.incrementAndGetIncarnationNumber();
        Gossip alive = new Gossip(GossipType.ALIVE, m);
        mergeItem(alive);
    }

    // TODO: more accurate name
    private void confirm(Member m) {
        Lock mutex = getMutex(m);
        mutex.lock();
        try {
            Gossip confirm = new Gossip(GossipType.CONFIRM, m);
            mergeItem(confirm);
        } finally {
            mutex.unlock();
        }
    }

    private void mergeItem(Gossip gossip) {
        boolean wasMerged = gossipBuffer.mergeItem(gossip);
        if (wasMerged) {
            handleSuspicionTimers(gossip);
            memberList.updateMemberState(gossip);
        }
    }

    private void handleSuspicionTimers(Gossip gossip) {
        switch (gossip.getGossipType()) {
            case ALIVE:
                cancelSuspectTimer(gossip.getMember());
                break;
            case SUSPECT:
                startSuspectTimer(gossip.getMember());
                break;
        }
    }

    private Lock getMutex(Member m) {
        Lock newMutexIfAbsent = new ReentrantLock();
        Lock existingMutex = mutexes.putIfAbsent(m, newMutexIfAbsent);
        return (existingMutex == null) ? newMutexIfAbsent : existingMutex;
    }

    private void startSuspectTimer(Member m) {
        if (suspectTimers.get(m) != null) return;

        ScheduledFuture<?> future = executorService.schedule(() -> {
            confirm(m);
        }, suspicionTimeout, TimeUnit.MILLISECONDS);
        suspectTimers.put(m, future);
    }

    private void cancelSuspectTimer(Member m) {
        ScheduledFuture<?> future = suspectTimers.get(m);
        if (future != null) {
            future.cancel(false);
            suspectTimers.remove(m);
        }
    }

}
