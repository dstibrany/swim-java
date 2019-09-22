import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class TestTransport implements Transport {
    private final Map<Member, BlockingQueue<Message>> failureDetectorQueues;
    private final Map<Member, Queue<Message>> listenerQueues;
    private final Member self;

    TestTransport(Member self, Map<Member, Queue<Message>> listenerQueues, Map<Member, BlockingQueue<Message>> failureDetectorQueues) {
        this.listenerQueues = listenerQueues;
        this.failureDetectorQueues = failureDetectorQueues;
        this.self = self;
    }

    @Override
    public Message receive() {
        BlockingQueue<Message> queue = failureDetectorQueues.get(self);
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void send(Message message) {
        Queue<Message> queue = getDestinationQueue(message.getMember());
        if (queue != null) {
            if (message.getIndirectProbeMember() != null) {
                queue.offer(new Message(message.getMessageType(), self, message.getIndirectProbeMember(), message.getGossipList()));
            } else {
                queue.offer(new Message(message.getMessageType(), self, message.getGossipList()));
            }
        } else {
            throw new RuntimeException("Queue not found");
        }
    }

    @Override
    public void close() {
    }

    private Queue<Message> getDestinationQueue(Member member) {
        if (listenerQueues.containsKey(member)) {
            return listenerQueues.get(member);
        } else {
            return failureDetectorQueues.getOrDefault(member, null);
        }
    }
}
