import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GossipBuffer {

    Map<Integer, Gossip> bufferElements = new HashMap<>();

    List<Gossip> getItems(int n) {
        return null;
    }

    void mergeItem(Gossip g) {

    }

    void incrementCount() {

    }

    static class GossipBufferElement {
        Gossip gossip;
        Lock lock = new ReentrantLock();


    }


}
