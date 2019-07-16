import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    private Member member;
    private Member indirectProbeMember;

    @BeforeEach
    void setUp() {
        member = new Member(1234, InetAddress.getLoopbackAddress());
        indirectProbeMember = new Member(1235, InetAddress.getLoopbackAddress());
    }

    private byte[] createMessageBytes(MessageType mt, Member indirectProbeMember) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(mt.getValue());
        if (indirectProbeMember != null) {
            dos.write(indirectProbeMember.getAddress().getAddress());
            dos.writeInt(indirectProbeMember.getPort());
        }
        return baos.toByteArray();
    }

    @Test
    void deserializePing() throws IOException {
        Message ping = Message.deserialize(createMessageBytes(MessageType.PING, null), member);
        assertEquals(MessageType.PING, ping.getMessageType());
    }

    @Test
    void deserializePingReq() throws IOException {
        Message pingReq = Message.deserialize(createMessageBytes(MessageType.PING_REQ, indirectProbeMember), member);
        assertEquals(MessageType.PING_REQ, pingReq.getMessageType());
        assertEquals(indirectProbeMember, pingReq.getIndirectProbeMember());
    }

    @Test
    void deserializeAck() throws IOException {
        Message ack = Message.deserialize(createMessageBytes(MessageType.ACK, null), member);
        assertEquals(MessageType.ACK, ack.getMessageType());
    }

    @Test
    void serializePing() throws IOException{
        Message ping = new Message(MessageType.PING, member);
        assertArrayEquals(createMessageBytes(MessageType.PING, null), ping.serialize());
    }

    @Test
    void serializePingReq() throws IOException {
        Message pingReq = new Message(MessageType.PING_REQ, member, indirectProbeMember);
        assertArrayEquals(createMessageBytes(MessageType.PING_REQ, indirectProbeMember), pingReq.serialize());
    }
}