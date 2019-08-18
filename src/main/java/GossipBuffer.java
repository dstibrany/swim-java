import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class GossipBuffer {
    private final Map<Member, Gossip> bufferElements = new ConcurrentHashMap<>();
    private final Map<Member, Lock> mutexes = new ConcurrentHashMap<>();

    List<Gossip> getItems(int n) {
        return null;
    }

    boolean mergeItem(Gossip gossip) {
        boolean merged = false;
        Lock mutex = getMutex(gossip.getMember());
        mutex.lock();
        try {
            if (gossip.overrides(bufferElements.get(gossip.getMember()))) {
                bufferElements.put(gossip.getMember(), gossip);
                merged = true;
            }
        } finally {
            mutex.unlock();
        }

        return merged;
    }

    private Lock getMutex(Member m) {
        Lock newLock = new ReentrantLock();
        Lock mutex = mutexes.putIfAbsent(m, newLock);
        return (mutex == null) ? newLock : mutex;
    }
}
