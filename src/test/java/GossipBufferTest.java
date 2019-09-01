import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GossipBufferTest {

    private GossipBuffer gossipBuffer;
    private ConcurrentHashMap<Member, Gossip> bufferElements;

    private ConcurrentHashMap<Member, Gossip> buildBuffer() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member m3 = new Member(1236, InetAddress.getLoopbackAddress());
        Member m4 = new Member(1237, InetAddress.getLoopbackAddress());
        Gossip g1 = new Gossip(GossipType.ALIVE, m1);
        Gossip g2 = new Gossip(GossipType.ALIVE, m2);
        Gossip g3 = new Gossip(GossipType.ALIVE, m3);
        Gossip g4 = new Gossip(GossipType.ALIVE, m4);
        ConcurrentHashMap<Member, Gossip> bufferElements = new ConcurrentHashMap<>();
        bufferElements.put(m1, g1);
        bufferElements.put(m2, g2);
        bufferElements.put(m3, g3);
        bufferElements.put(m4, g4);
        return bufferElements;
    }

    @BeforeEach
    void setUp() {
        bufferElements = new ConcurrentHashMap<>();
        gossipBuffer = new GossipBuffer(bufferElements);
    }

    @Test
    void getItemsReturnsCorrectNumberOfItems() {
        GossipBuffer gb = new GossipBuffer(buildBuffer());

        List<Gossip> gossipList1 = gb.getItems(2, 4);
        assertEquals(2, gossipList1.size());

        List<Gossip> gossipList2 = gb.getItems(4, 4);
        assertEquals(4, gossipList2.size());

        List<Gossip> gossipList3 = gb.getItems(5, 4);
        assertEquals(4, gossipList3.size());
    }

    @Test
    void getItemsExcludesExpiredGossip() {
        ConcurrentHashMap<Member, Gossip> bufferElements = buildBuffer();
        bufferElements.values().stream().findAny().get().setExpired();
        GossipBuffer gb = new GossipBuffer(bufferElements);

        List<Gossip> gossipList = gb.getItems(bufferElements.size(), bufferElements.size());
        assertEquals(bufferElements.size() - 1, gossipList.size());
        for (Gossip g : gossipList) {
            assertFalse(g.isExpired());
        }
    }
    @Test
    void getItemsReturnsCorrectOrder() {
        ConcurrentHashMap<Member, Gossip> bufferElements = buildBuffer();
        List<Gossip> gossip = bufferElements.values().stream().limit(2).collect(Collectors.toList());
        // increase piggyback count
        for (int i = 0; i < gossip.size(); i++) {
            for (int j = 0; j < i + 1; j++) {
                gossip.get(i).serialize();
            }
        }
        GossipBuffer gb = new GossipBuffer(bufferElements);

        List<Gossip> gossipList = gb.getItems(bufferElements.size(), bufferElements.size());
        assertEquals(gossip.get(1), gossipList.get(gossipList.size() - 1));
        assertEquals(gossip.get(0), gossipList.get(gossipList.size() - 2));
    }

    @Test
    void getItemsExpiresGossip() {
        ConcurrentHashMap<Member, Gossip> bufferElements = buildBuffer();

        GossipBuffer gb = new GossipBuffer(bufferElements);

        assertFalse(true);
    }

    @Test
    void itemMerged() {
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Member, Gossip> bufferElements = mock(ConcurrentHashMap.class);
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