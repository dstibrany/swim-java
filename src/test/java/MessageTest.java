import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private Message ping;
    private Message ping_req;
    private Message ack;
    private InetAddress srcAddress;
    private InetAddress dstAddress;
    private int dstPort;
    private int srcPort;

    @BeforeEach
    void setUp() {
        srcPort = 2222;
        dstPort = 3333;
        srcAddress = InetAddress.getLoopbackAddress();
        dstAddress = InetAddress.getLoopbackAddress();

        ping = new Message(MessageType.PING, dstPort, dstAddress);
        ping_req = new Message(MessageType.PING_REQ, dstPort, dstAddress);
        ack = new Message(MessageType.ACK, dstPort, dstAddress);
    }

    @Test
    void getMessageType() {
        assertEquals(MessageType.PING, ping.getMessageType());
        assertEquals(MessageType.PING_REQ, ping_req.getMessageType());
        assertEquals(MessageType.ACK, ack.getMessageType());
    }

    @Test
    void getSrcPort() throws IOException {
        Message m = new Message(new byte[256], srcPort, srcAddress);
        assertEquals(srcPort, m.getSrcPort());
    }

    @Test
    void getDstPort() throws IOException {
        Message m = new Message(MessageType.ACK, dstPort, dstAddress);
        assertEquals(dstPort, m.getDstPort());
    }

    @Test
    void getSrcAddress() throws IOException {
        Message m = new Message(new byte[256], srcPort, srcAddress);
        assertEquals(srcAddress, m.getSrcAddress());
    }

    @Test
    void getDstAddress() {
        Message m = new Message(MessageType.PING, dstPort, dstAddress);
        assertEquals(dstAddress, m.getDstAddress());
    }

    @Test
    void serialize() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(MessageType.PING.getValue());
        byte[] buffer = bb.array();

        Message m1 = new Message(MessageType.PING, dstPort, dstAddress);

        assertArrayEquals(buffer, m1.serialize());
    }

    @Test
    void deserialize() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(MessageType.PING.getValue());
        byte[] buffer = bb.array();
        Message m = new Message(buffer, srcPort, srcAddress);

        assertEquals(MessageType.PING, m.getMessageType());
    }

    @Test
    void messageTypeGetType() {
        assertEquals(MessageType.PING, MessageType.getType(1));
        assertEquals(MessageType.PING_REQ, MessageType.getType(2));
        assertEquals(MessageType.ACK, MessageType.getType(3));
    }

    @Test
    void messageTypeGetValue() {
        assertEquals(1, MessageType.PING.getValue());
        assertEquals(2, MessageType.PING_REQ.getValue());
        assertEquals(3, MessageType.ACK.getValue());
    }
}