import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GossipBuffer {
    Map<Member, Gossip> bufferElements = new HashMap<>();

    List<Gossip> getItems(int n) {
        return null;
    }

    void mergeItem(Gossip g) {
        Lock mutex = g.getMember().getMutex();
        mutex.lock();
        try {

        } finally {
            mutex.unlock();
        }
    }

    void incrementCount() {

    }

    static class GossipBufferElement {
        Gossip gossip;
        Lock lock = new ReentrantLock();


    }


}
