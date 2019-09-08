import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GossipTest {
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(1234, InetAddress.getLoopbackAddress());
    }

    @Test
    void deserializeAlive() {
        Gossip alive = new Gossip(GossipType.ALIVE, member);
        byte[] bytes = alive.serialize();
        assertEquals(alive, Gossip.deserialize(bytes));
    }

    @Test
    void deserializeSuspect() {
        Gossip suspect = new Gossip(GossipType.SUSPECT, member);
        byte[] bytes = suspect.serialize();
        assertEquals(suspect, Gossip.deserialize(bytes));
    }

    @Test
    void deserializeConfirm() throws IOException {
        Gossip confirm = new Gossip(GossipType.CONFIRM, member);
        byte[] bytes = confirm.serialize();
        assertEquals(confirm, Gossip.deserialize(bytes));
    }

    @Test
    void deserializeUnknown() {
        Gossip unknown = new Gossip(GossipType.UNKNOWN, member);
        byte[] bytes = unknown.serialize();
        assertEquals(unknown, Gossip.deserialize(bytes));
    }

    @Test
    void checkBytes() {
        Gossip alive = new Gossip(GossipType.ALIVE, member);
        assertEquals(Gossip.BYTES, alive.serialize().length);
    }

    @Test
    void testEqualsAndHashCode() {
        Gossip g1 = new Gossip(GossipType.ALIVE, member);
        Gossip g2 = new Gossip(GossipType.ALIVE, member);
        assertTrue(g1.equals(g2) && g2.equals(g1));
        assertEquals(g1.hashCode(), g2.hashCode());
    }

}