import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {
    private Member member;
    private Member indirectProbeMember;
    private List<Gossip> gossipList;

    @BeforeEach
    void setUp() {
        member = new Member(1234, InetAddress.getLoopbackAddress());
        indirectProbeMember = new Member(1235, InetAddress.getLoopbackAddress());
        gossipList = new ArrayList<>();
    }

    private byte[] createMessageBytes(MessageType mt, Member indirectProbeMember, List<Gossip> gossipList)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(mt.getValue());
        if (indirectProbeMember != null) {
            dos.write(indirectProbeMember.serialize());
        }
        dos.writeInt(gossipList.size());
        for (Gossip g : gossipList) {
            dos.write(g.serialize());
        }
        return baos.toByteArray();
    }

    @Test
    void deserializePing() throws IOException {
        Message ping = Message.deserialize(
                createMessageBytes(MessageType.PING, null, gossipList),
                member
        );
        assertEquals(MessageType.PING, ping.getMessageType());
    }

    @Test
    void deserializePingReq() throws IOException {
        Message pingReq = Message.deserialize(
                createMessageBytes(MessageType.PING_REQ, indirectProbeMember, gossipList),
                member
        );
        assertEquals(MessageType.PING_REQ, pingReq.getMessageType());
        assertEquals(indirectProbeMember, pingReq.getIndirectProbeMember());
    }

    @Test
    void deserializeAck() throws IOException {
        Message ack = Message.deserialize(
                createMessageBytes(MessageType.ACK, null, gossipList),
                member
        );
        assertEquals(MessageType.ACK, ack.getMessageType());
    }

    @Test
    void deserializeUnknown() {
        Message unknown = Message.deserialize(new byte[256], member);
        assertEquals(MessageType.UNKNOWN, unknown.getMessageType());
    }

    @Test
    void serializePing() throws IOException {
        Message ping = new Message(MessageType.PING, member, gossipList);
        assertArrayEquals(
                createMessageBytes(MessageType.PING, null, gossipList),
                ping.serialize()
        );
    }

    @Test
    void serializePingReq() throws IOException {
        Message pingReq = new Message(MessageType.PING_REQ, member, indirectProbeMember, gossipList);
        assertArrayEquals(
                createMessageBytes(MessageType.PING_REQ, indirectProbeMember, gossipList),
                pingReq.serialize()
        );
    }

    @Test
    void serializeGossip() throws IOException {
        Member gossipMember = new Member(8888, InetAddress.getLoopbackAddress());
        gossipList.add(new Gossip(GossipType.ALIVE, gossipMember));
        Message ping = new Message(MessageType.PING, member, indirectProbeMember, gossipList);
        assertArrayEquals(
                createMessageBytes(MessageType.PING, null, gossipList),
                ping.serialize()
        );
    }

    @Test
    void deserializeGossip() throws IOException {
        Member gossipMember = new Member(8888, InetAddress.getLoopbackAddress());
        gossipList.add(new Gossip(GossipType.ALIVE, gossipMember));
        Message ping = Message.deserialize(
                createMessageBytes(MessageType.PING, null, gossipList),
                member
        );

        assertEquals(gossipList, ping.getGossipList());
    }
}