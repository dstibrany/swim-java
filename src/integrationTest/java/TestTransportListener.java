import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;

public class TestTransportListener implements Transport {
    private final Queue<Message> receiveQueue;

    TestTransportListener(int port, Queue<Message> receiveQueue) {
        this.receiveQueue = receiveQueue;
    }

    @Override
    public Message receive() {
        Message message = receiveQueue.poll();
        if (message == null) {
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
}
