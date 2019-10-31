package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Queue;
import java.util.Random;

public class TestTransportListener implements Transport {
    private final Queue<Message> receiveQueue;
    private final double dropProbability;
    private final Random random = new Random();
    private final Logger logger = LogManager.getLogger();

    TestTransportListener(int port, Queue<Message> receiveQueue, double dropProbability) {
        this.receiveQueue = receiveQueue;
        this.dropProbability = dropProbability;
    }

    @Override
    public Message receive() {
        Message message = receiveQueue.poll();

        if (message == null || shouldDropMessage(message)) {
            // We want poll() to return null if the queue is empty, but we cannot pass a null message to higher layers
            // so we'll just create a new message of type UNKNOWN which will act as a NO-OP.
            return new Message(MessageType.UNKNOWN, null, Collections.emptyList());
        } else {
            return message;
        }
    }

    @Override
    public void send(Message message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
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
