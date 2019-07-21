import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTypeTest {

    @Test
    void getType() {
        assertEquals(MessageType.PING, MessageType.getType(1));
        assertEquals(MessageType.PING_REQ, MessageType.getType(2));
        assertEquals(MessageType.ACK, MessageType.getType(3));
        assertEquals(MessageType.UNKNOWN, MessageType.getType(55));
        assertEquals(MessageType.UNKNOWN, MessageType.getType(67));
    }

    @Test
    void getValue() {
        assertEquals(1, MessageType.PING.getValue());
        assertEquals(2, MessageType.PING_REQ.getValue());
        assertEquals(3, MessageType.ACK.getValue());
    }
}