package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class TestTransport implements Transport {
    private final Map<Member, BlockingQueue<Message>> failureDetectorQueues;
    private final Map<Member, Queue<Message>> listenerQueues;
    private final Member self;
    private final double dropProbability;
    private final Random random = new Random();
    private final Logger logger = LogManager.getLogger();

    TestTransport(Member self,
                  Map<Member, Queue<Message>> listenerQueues,
                  Map<Member, BlockingQueue<Message>> failureDetectorQueues,
                  double dropProbability) {
        this.listenerQueues = listenerQueues;
        this.failureDetectorQueues = failureDetectorQueues;
        this.self = self;
        this.dropProbability = dropProbability;
    }

    @Override
    public Message receive() {
        BlockingQueue<Message> queue = failureDetectorQueues.get(self);
        try {
            Message message = queue.take();
            return shouldDropMessage(message) ? receive() : message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void send(Message message) {
        if (shouldDropMessage(message)) {
            return;
        }

        Queue<Message> queue = getDestinationQueue(message.getMember());
        if (queue != null) {
            message.serialize(); // only used to increase piggybackcount
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

    private boolean shouldDropMessage(Message m) {
        if (m.getMessageType() == MessageType.JOIN || m.getMessageType() == MessageType.JOIN_ACK) return false;
        if (random.nextDouble() < this.dropProbability) {
            logger.debug("Dropping message: {}", m.getMessageType());
            return true;
        }
        return false;
    }
}
