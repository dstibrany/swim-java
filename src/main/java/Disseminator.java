import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Disseminator {
    private final int maxGossipPerMessage;
    private MemberList memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer(new ConcurrentHashMap<>());
    private final Map<Member, Lock> mutexes = new ConcurrentHashMap<>();
    private final Logger logger = LogManager.getLogger();

    Disseminator(MemberList memberList, Config conf) {
        this.memberList = memberList;
        maxGossipPerMessage = conf.getMaxGossipPerMessage();
    }

    List<Gossip> generateMemberList() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member member : memberList.getList()) {
            gossipList.add(new Gossip(GossipType.ALIVE, member, 0));
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
//                    refute();
                }
                Member member = memberList.get(gossip.getMember());
                if (member == null && gossip.getGossipType() != GossipType.JOIN) return;

                boolean wasMerged = gossipBuffer.mergeItem(gossip);
                if (wasMerged) {
                    updateMember(member, gossip.getGossipType());
                }
            } finally {
                mutex.unlock();
            }
        }
    }

    void alive() {
//        Gossip alive = new Gossip(GossipType.ALIVE, SwimJava.getSelf(), SwimJava.getSelf());
//        gossipBuffer.mergeItem(alive);

    }

    void suspect(Member m) {
        Gossip suspect = new Gossip(GossipType.SUSPECT, m, m.getIncarnationNumber());
        mergeGossip(Arrays.asList(suspect));
    }

    void confirm(Member m) {
        Gossip confirm = new Gossip(GossipType.CONFIRM, m, m.getIncarnationNumber());
        mergeGossip(Arrays.asList(confirm));
    }

    void updateMember(Member member, GossipType gossipType) {
//        if (gossip.getMember().getIncarnationNumber() > member.getIncarnationNumber()) {
//
//        }
        switch (gossipType) {
            case ALIVE:
                member.alive();
                break;
            case SUSPECT:
                member.suspect();
                break;
            case CONFIRM:
                memberList.remove(member);
                break;
            case JOIN:
                memberList.add(member); // TODO: needs actual member
                break;
        }
    }

    private Lock getMutex(Member m) {
        Lock newMutexIfAbsent = new ReentrantLock();
        Lock existingMutex = mutexes.putIfAbsent(m, newMutexIfAbsent);
        return (existingMutex == null) ? newMutexIfAbsent : existingMutex;
    }

}
