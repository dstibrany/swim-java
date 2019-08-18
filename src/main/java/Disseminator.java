import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Disseminator {
    private List<Member> memberList;
    private GossipBuffer gossipBuffer = new GossipBuffer(new ConcurrentHashMap<>());
    private final Map<Member, Lock> mutexes = new ConcurrentHashMap<>();

    Disseminator(List<Member> memberList) {
        this.memberList = memberList;
    }

    // TODO: get this working
    List<Gossip> generateGossip() {
        return gossipBuffer.getItems(6);
    }

    List<Gossip> sendMemberList() {
        List<Gossip> gossipList = new ArrayList<>();
        for (Member member : memberList) {
            gossipList.add(new Gossip(GossipType.JOIN, member, 0));
        }
        return gossipList;
    }

    void mergeGossip(List<Gossip> gossipList) {
        for (Gossip gossip : gossipList) {
            Lock mutex = getMutex(gossip.getMember());
            mutex.lock();
            try {
                boolean wasMerged = gossipBuffer.mergeItem(gossip);
                if (wasMerged) {
                    Member member = memberList.contains(gossip.getMember())
                            ? memberList.get(memberList.indexOf(gossip.getMember()))
                            : gossip.getMember();
                    updateMember(member, gossip.getGossipType());
                }
            } finally {
                mutex.unlock();
            }
        }
    }

    void updateMember(Member member, GossipType gossipType) {
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
                memberList.add(member);
                break;
        }
    }

    private Lock getMutex(Member m) {
        Lock newMutexIfAbsent = new ReentrantLock();
        Lock existingMutex = mutexes.putIfAbsent(m, newMutexIfAbsent);
        return (existingMutex == null) ? newMutexIfAbsent : existingMutex;
    }

}
