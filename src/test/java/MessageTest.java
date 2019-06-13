import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private Message ping;
    private Message ping_req;
    private Message ack;

    @BeforeEach
    void setUp() {
        ping = new Message(Message.MessageType.PING);
        ping_req = new Message(Message.MessageType.PING_REQ);
        ack = new Message(Message.MessageType.ACK);
    }

    @Test
    void getMessageType() {
        assertEquals(Message.MessageType.PING, ping.getMessageType());
        assertEquals(Message.MessageType.PING_REQ, ping_req.getMessageType());
        assertEquals(Message.MessageType.ACK, ack.getMessageType());
    }

    @Test
    void getPort() {
    }

    @Test
    void getAddress() {
    }

    @Test
    void serialize() {
    }

    @Test
    void deserialize() {
    }
}