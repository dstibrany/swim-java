import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DisseminatorTest {

    private GossipBuffer gossipBuffer;
    private MemberList memberList;
    private Config config;
    private Disseminator disseminator;

    @BeforeEach
    void setUp() {
        com.typesafe.config.Config mergedConf = ConfigFactory
                .parseString("swim-java.protocol_period=10, swim-java.request_timeout=10")
                .withFallback(ConfigFactory.defaultReference());
        config = new Config(mergedConf);
        gossipBuffer = mock(GossipBuffer.class);
        memberList = mock(MemberList.class);
        disseminator = new Disseminator(memberList, gossipBuffer, config);
    }

    @Test
    void generateMemberList() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member self = new Member(1236, InetAddress.getLoopbackAddress());
        MemberList m = new MemberList(new HashSet<>(Arrays.asList(m1, m2)), self);
        Disseminator d = new Disseminator(m, gossipBuffer, config);

        List<Gossip> gossipList = d.generateMemberList();

        assertEquals(m.size(), gossipList.size());
        for (Gossip g : gossipList) {
            assertEquals(GossipType.ALIVE, g.getGossipType());
        }
        assertEquals(m.getAsList(), gossipList.stream().map(Gossip::getMember).collect(Collectors.toList()));
    }

    @Test
    void mergeMemberList() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member self = new Member(1236, InetAddress.getLoopbackAddress());
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1, m2)), self);
        MemberList ml2 = new MemberList(self);
        Disseminator d1 = new Disseminator(ml1, gossipBuffer, config);
        Disseminator d2 = new Disseminator(ml2, gossipBuffer, config);
        List<Gossip> gossipList = d1.generateMemberList();

        d2.mergeMemberList(gossipList);

        assertEquals(ml1.getAsList(), ml2.getAsList());
    }

    @Test
    void generateGossip() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member self = new Member(1236, InetAddress.getLoopbackAddress());
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1, m2)), self);
        Disseminator d = new Disseminator(ml1, gossipBuffer, config);

        d.generateGossip();

        verify(gossipBuffer).getItems(config.getMaxGossipPerMessage(), ml1.size());
    }

    @Test
    void mergeGossip() {
    }

    @Test
    void createSuspectGossip() {
    }
}