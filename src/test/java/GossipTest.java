import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GossipTest {
    private Member member;
    private int incarnationNumber;

    @BeforeEach
    void setUp() {
        member = new Member(1234, InetAddress.getLoopbackAddress());
        incarnationNumber = 0;
    }

    private byte[] createMessageBytes(GossipType type) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(type.getValue());
        dos.write(member.getAddress().getAddress());
        dos.writeInt(member.getPort());
        dos.writeInt(incarnationNumber);
        return baos.toByteArray();
    }

    @Test
    void deserializeAlive() throws IOException {
        Gossip alive = Gossip.deserialize(createMessageBytes(GossipType.ALIVE));
        assertEquals(GossipType.ALIVE, alive.getGossipType());
    }

    @Test
    void deserializeSuspect() throws IOException {
        Gossip suspect = Gossip.deserialize(createMessageBytes(GossipType.SUSPECT));
        assertEquals(GossipType.SUSPECT, suspect.getGossipType());
    }

    @Test
    void deserializeConfirm() throws IOException {
        Gossip confirm = Gossip.deserialize(createMessageBytes(GossipType.CONFIRM));
        assertEquals(GossipType.CONFIRM, confirm.getGossipType());
    }

    @Test
    void deserializeUnknown() {
        Gossip unknown = Gossip.deserialize(new byte[256]);
        assertEquals(GossipType.UNKNOWN, unknown.getGossipType());
    }

    @Test
    void serializeAlive() throws IOException {
        Gossip alive = new Gossip(GossipType.ALIVE, member);
        assertArrayEquals(createMessageBytes(GossipType.ALIVE), alive.serialize());
    }
}