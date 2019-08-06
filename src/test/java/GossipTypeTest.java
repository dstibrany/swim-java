import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GossipTypeTest {

    @Test
    void getType() {
        assertEquals(GossipType.ALIVE, GossipType.getType(1));
        assertEquals(GossipType.SUSPECT, GossipType.getType(2));
        assertEquals(GossipType.CONFIRM, GossipType.getType(3));
        assertEquals(GossipType.UNKNOWN, GossipType.getType(55));
        assertEquals(GossipType.UNKNOWN, GossipType.getType(67));
    }

    @Test
    void getValue() {
        assertEquals(1, GossipType.ALIVE.getValue());
        assertEquals(2, GossipType.SUSPECT.getValue());
        assertEquals(3, GossipType.CONFIRM.getValue());
    }
}

