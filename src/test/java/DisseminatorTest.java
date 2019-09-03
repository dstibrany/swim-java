import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisseminatorTest {

    private GossipBuffer gossipBuffer;
    private MemberList memberList;
    private Config config;
    private Disseminator disseminator;
    private Member self;

    @BeforeEach
    void setUp() {
        config = mock(Config.class);
        gossipBuffer = mock(GossipBuffer.class);
        memberList = mock(MemberList.class);
        disseminator = new Disseminator(memberList, gossipBuffer, config);
        self = new Member(5555, InetAddress.getLoopbackAddress());

        when(config.getMaxGossipPerMessage()).thenReturn(6);
        when(config.getSelf()).thenReturn(self);
        when(config.getSuspicionTimeout()).thenReturn(5000);
    }

    @Test
    void generateMemberList() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
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
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1, m2)), self);
        Disseminator d = new Disseminator(ml1, gossipBuffer, config);

        d.generateGossip();

        verify(gossipBuffer).getItems(config.getMaxGossipPerMessage(), ml1.size());
    }

    @Test
    void doNotMergeIfUnknownMember() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member unknown = new Member(1235, InetAddress.getLoopbackAddress());
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1)), self);
        Disseminator d = new Disseminator(ml1, gossipBuffer, config);
        Gossip g = new Gossip(GossipType.ALIVE, unknown);

        d.mergeGossip(Arrays.asList(g));

        verify(gossipBuffer, never()).mergeItem(g);
    }

    @Test
    void mergeIfUnknownMemberWithJoinGossip() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member unknown = new Member(1235, InetAddress.getLoopbackAddress());
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1)), self);
        Disseminator d = new Disseminator(ml1, gossipBuffer, config);
        Gossip g = new Gossip(GossipType.JOIN, unknown);

        d.mergeGossip(Arrays.asList(g));

        verify(gossipBuffer).mergeItem(g);
    }

    @Test
    void createAliveGossipIfSelfIsSuspected() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        MemberList ml1 = new MemberList(new HashSet<>(Arrays.asList(m1)), self);
        Disseminator d = new Disseminator(ml1, gossipBuffer, config);
        ArgumentCaptor<Gossip> argument = ArgumentCaptor.forClass(Gossip.class);
        Gossip g = new Gossip(GossipType.SUSPECT, self);
        int originalIncarnationNumber = self.getIncarnationNumber();

        d.mergeGossip(Collections.singletonList(g));

        verify(gossipBuffer).mergeItem(argument.capture());
        Gossip alive = argument.getValue();
        assertEquals(GossipType.ALIVE, alive.getGossipType());
        assertEquals(self, alive.getMember());
        assertEquals(originalIncarnationNumber + 1, alive.getIncarnationNumber());
    }

    @Test
    void mergeGossip() {
    }

    @Test
    void createSuspectGossip() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        when(gossipBuffer.mergeItem(any(Gossip.class))).thenReturn(true);
        ArgumentCaptor<Gossip> argument = ArgumentCaptor.forClass(Gossip.class);

        disseminator.createSuspectGossip(m1);

        verify(gossipBuffer).mergeItem(argument.capture());
        verify(memberList).updateMemberState(argument.capture());
        List<Gossip> arguments = argument.getAllValues();
        for (Gossip g : arguments) {
            assertEquals(GossipType.SUSPECT, g.getGossipType());
            assertEquals(m1, g.getMember());
        }
    }
}