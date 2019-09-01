import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GossipBufferTest {

    private GossipBuffer gossipBuffer;
    private ConcurrentHashMap<Member, Gossip> bufferElements;

    @BeforeEach
    void setUp() {
        bufferElements = new ConcurrentHashMap<>();
        gossipBuffer = new GossipBuffer(bufferElements);
    }

    @Test
    void getItems() {
    }

    @Test
    void itemMerged() {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Member, Gossip> bufferElements =  mock(ConcurrentHashMap.class);
        GossipBuffer gossipBuffer = new GossipBuffer(bufferElements);
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress());
        Gossip alive = new Gossip(GossipType.ALIVE, m1);

        boolean wasMerged = gossipBuffer.mergeItem(alive);

        verify(bufferElements).put(m1, alive);
        assertTrue(wasMerged);
    }

    @Test
    void itemNotMerged() {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Member, Gossip> bufferElements = spy(ConcurrentHashMap.class);
        GossipBuffer gossipBuffer = new GossipBuffer(bufferElements);
        Member m0 = new Member(5555, InetAddress.getLoopbackAddress());
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress(), 1);
        Gossip alive_inc0 = new Gossip(GossipType.ALIVE, m0);
        Gossip alive_inc1 = new Gossip(GossipType.ALIVE, m1);
        bufferElements.put(m1, alive_inc1);

        boolean wasMerged = gossipBuffer.mergeItem(alive_inc0);

        verify(bufferElements, never()).put(m1, alive_inc0);
        assertFalse(wasMerged);
    }

    @Test
    void overridesOnDifferentMembersThrowsException() {
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress());
        Member m2 = new Member(5556, InetAddress.getLoopbackAddress());
        Gossip alive0 = new Gossip(GossipType.ALIVE, m1);
        Gossip alive1 = new Gossip(GossipType.ALIVE, m2);
        assertThrows(RuntimeException.class, () -> {
            gossipBuffer.overrides(alive0, alive1);
        });
    }

    @Test
    void aliveOverrides() {
        Member m0 = new Member(5555, InetAddress.getLoopbackAddress());
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress(), 1);
        Gossip alive1 = new Gossip(GossipType.ALIVE, m1);
        Gossip alive0 = new Gossip(GossipType.ALIVE, m0);
        Gossip suspect0 = new Gossip(GossipType.SUSPECT, m0);
        Gossip confirm = new Gossip(GossipType.CONFIRM, m1);
        Gossip join = new Gossip(GossipType.JOIN, m1);

        assertTrue(gossipBuffer.overrides(alive1, alive0));
        assertTrue(gossipBuffer.overrides(alive1, suspect0));
        assertFalse(gossipBuffer.overrides(alive0, alive1));
        assertFalse(gossipBuffer.overrides(suspect0, alive1));
        assertFalse(gossipBuffer.overrides(alive1, confirm));
        assertFalse(gossipBuffer.overrides(alive1, join));
    }

    @Test
    void suspectOverrides() {
        Member m0 = new Member(5555, InetAddress.getLoopbackAddress());
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress(), 1);
        Gossip suspect_inc1 = new Gossip(GossipType.SUSPECT, m1);
        Gossip suspect_inc0 = new Gossip(GossipType.SUSPECT, m0);
        Gossip alive_inc0 = new Gossip(GossipType.ALIVE, m0);
        Gossip alive_inc1 = new Gossip(GossipType.ALIVE, m1);
        Gossip confirm = new Gossip(GossipType.CONFIRM, m1);
        Gossip join = new Gossip(GossipType.JOIN, m1);

        assertTrue(gossipBuffer.overrides(suspect_inc1, suspect_inc0));
        assertTrue(gossipBuffer.overrides(suspect_inc1, alive_inc0));
        assertTrue(gossipBuffer.overrides(suspect_inc1, alive_inc1));
        assertFalse(gossipBuffer.overrides(suspect_inc0, suspect_inc1));
        assertFalse(gossipBuffer.overrides(alive_inc0, alive_inc1));
        assertFalse(gossipBuffer.overrides(suspect_inc1, confirm));
        assertFalse(gossipBuffer.overrides(suspect_inc1, join));
    }

    @Test
    void confirmOverrides() {
        Member m0 = new Member(5555, InetAddress.getLoopbackAddress());
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress(), 1);
        Gossip suspect_inc1 = new Gossip(GossipType.SUSPECT, m1);
        Gossip suspect_inc0 = new Gossip(GossipType.SUSPECT, m0);
        Gossip alive_inc0 = new Gossip(GossipType.ALIVE, m0);
        Gossip alive_inc1 = new Gossip(GossipType.ALIVE, m1);
        Gossip confirm = new Gossip(GossipType.CONFIRM, m1);
        Gossip join = new Gossip(GossipType.JOIN, m1);

        assertTrue(gossipBuffer.overrides(confirm, suspect_inc0));
        assertTrue(gossipBuffer.overrides(confirm, suspect_inc1));
        assertTrue(gossipBuffer.overrides(confirm, alive_inc0));
        assertTrue(gossipBuffer.overrides(confirm, alive_inc1));
        assertFalse(gossipBuffer.overrides(confirm, join));
        assertFalse(gossipBuffer.overrides(alive_inc0, confirm));
        assertFalse(gossipBuffer.overrides(suspect_inc1, confirm));
        assertFalse(gossipBuffer.overrides(suspect_inc0, confirm));
    }

    @Test
    void joinOverrides() {
        Member m1 = new Member(5555, InetAddress.getLoopbackAddress());
        Gossip suspect_inc1 = new Gossip(GossipType.SUSPECT, m1);
        Gossip suspect_inc0 = new Gossip(GossipType.SUSPECT, m1);
        Gossip alive_inc0 = new Gossip(GossipType.ALIVE, m1);
        Gossip alive_inc1 = new Gossip(GossipType.ALIVE, m1);
        Gossip confirm = new Gossip(GossipType.CONFIRM, m1);
        Gossip join = new Gossip(GossipType.JOIN, m1);

        assertTrue(gossipBuffer.overrides(join, suspect_inc0));
        assertTrue(gossipBuffer.overrides(join, suspect_inc1));
        assertTrue(gossipBuffer.overrides(join, alive_inc0));
        assertTrue(gossipBuffer.overrides(join, alive_inc1));
        assertTrue(gossipBuffer.overrides(join, confirm));
        assertFalse(gossipBuffer.overrides(confirm, join));
        assertFalse(gossipBuffer.overrides(alive_inc0, join));
        assertFalse(gossipBuffer.overrides(suspect_inc1, join));
        assertFalse(gossipBuffer.overrides(suspect_inc0, join));
    }
}