package ca.davestibrany.swimjava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void deserializePing() {
        Message ping = new Message(MessageType.PING, member, gossipList);
        byte[] bytes = ping.serialize();
        assertEquals(ping, Message.deserialize(bytes, member));
    }

    @Test
    void deserializePingReq() {
        Message pingReq = new Message(MessageType.PING_REQ, member, indirectProbeMember, gossipList);
        byte[] bytes = pingReq.serialize();
        assertEquals(pingReq, Message.deserialize(bytes, member));
    }

    @Test
    void deserializeAck() {
        Message ack = new Message(MessageType.ACK, member, gossipList);
        byte[] bytes = ack.serialize();
        assertEquals(ack, Message.deserialize(bytes, member));
    }

    @Test
    void deserializeUnknown() {
        Message unknown = new Message(MessageType.UNKNOWN, member, gossipList);
        byte[] bytes = unknown.serialize();
        assertEquals(unknown, Message.deserialize(bytes, member));
    }

    @Test
    void deserializeGossip() {
        Member gossipMember = new Member(8888, InetAddress.getLoopbackAddress());
        gossipList.add(new Gossip(GossipType.ALIVE, gossipMember));
        Message ping = new Message(MessageType.PING, member, gossipList);
        byte[] bytes = ping.serialize();
        assertEquals(ping, Message.deserialize(bytes, member));
    }

    @Test
    void testEqualsAndHashCode() {
        Message ping1 = new Message(MessageType.PING, member, gossipList);
        Message ping2 = new Message(MessageType.PING, member, gossipList);

        assertTrue(ping1.equals(ping2) && ping2.equals(ping1));
        assertEquals(ping1.hashCode(), ping2.hashCode());
    }
}