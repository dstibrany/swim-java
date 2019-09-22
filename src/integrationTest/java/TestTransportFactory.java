import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class TestTransportFactory implements TransportFactory {
    private final Member self;
    private Map<Member, Queue<Message>> listenerQueue;
    private Map<Member, BlockingQueue<Message>> failureDetectorQueue;

    TestTransportFactory(Member self, Map<Member, Queue<Message>> listenerQueue, Map<Member, BlockingQueue<Message>> failureDetectorQueue) {
        this.self = self;
        this.listenerQueue = listenerQueue;
        this.failureDetectorQueue = failureDetectorQueue;
    }

    @Override
    public Transport create() {
        Random rand = new Random();
        Member member = new Member(rand.nextInt(Integer.MAX_VALUE), self.getAddress());
        BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
        failureDetectorQueue.put(member, queue);
        return new TestTransport(member, listenerQueue, failureDetectorQueue);
    }

    @Override
    public Transport createListener(int port) {
        Queue<Message> queue = new LinkedBlockingQueue<>();
        listenerQueue.put(self, queue);
        return new TestTransportListener(port, queue);
    }
}
