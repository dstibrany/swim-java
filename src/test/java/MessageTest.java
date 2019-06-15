import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;

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
    void getPort() throws IOException {
        int port = 2222;
        Message m = new Message(new byte[256], port, InetAddress.getLoopbackAddress());
        assertEquals(port, m.getPort());
    }

    @Test
    void getAddress() throws IOException {
        Message m = new Message(new byte[256], 2222, InetAddress.getLoopbackAddress());
        assertEquals(InetAddress.getLoopbackAddress(), m.getAddress());
    }

    @Test
    void serialize() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(Message.MessageType.PING.getValue());
        byte[] buffer = bb.array();

        Message m1 = new Message(Message.MessageType.PING);

        assertArrayEquals(buffer, m1.serialize());
    }

    @Test
    void deserialize() throws IOException {
        int port = 2222;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(Message.MessageType.PING.getValue());
        byte[] buffer = bb.array();

        Message m1 = new Message(Message.MessageType.PING);
        Message m2 = new Message(buffer, port, InetAddress.getLoopbackAddress());

        assertEquals(m1.getMessageType(), m2.getMessageType());
    }

    @Test
    void messageTypeGetType() {
        assertEquals(Message.MessageType.PING, Message.MessageType.getType(1));
        assertEquals(Message.MessageType.PING_REQ, Message.MessageType.getType(2));
        assertEquals(Message.MessageType.ACK, Message.MessageType.getType(3));
    }

    @Test
    void messageTypeGetValue() {
        assertEquals(1, Message.MessageType.PING.getValue());
        assertEquals(2, Message.MessageType.PING_REQ.getValue());
        assertEquals(3, Message.MessageType.ACK.getValue());
    }
}