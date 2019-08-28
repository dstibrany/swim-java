import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisseminatorTest {

    private Disseminator disseminator;
    private MemberList memberList;
    private Member member;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        memberList = mock(MemberList.class);
        disseminator = new Disseminator(memberList, new Config());
    }

    @Test
    void generateGossip() {
    }

    @Test
    void sendMemberList() {
//        Member m1 =
//        List<Member> memberList = new ArrayList<>();
//        Disseminator d = new Disseminator(memberList);
//
//        List<Gossip> gossipList = d.sendMemberList();
//
//        assertEquals(memberList.size(), gossipList.size());
    }

    @Test
    void mergeGossip() {
    }

    @Test
    void updateMemberWithAlive() {
        disseminator.updateMemberState(member, new Gossip(GossipType.ALIVE, member, 0));

        verify(member).alive();
        verifyZeroInteractions(memberList);
    }

    @Test
    void updateMemberWithSuspect() {
        disseminator.updateMemberState(member, new Gossip(GossipType.SUSPECT, member, 0));

        verify(member).suspect();
        verifyZeroInteractions(memberList);
    }

    @Test
    void updateMemberWithConfirm() {
        disseminator.updateMemberState(member, new Gossip(GossipType.CONFIRM, member, 0));

        verify(memberList).remove(member);
    }

    @Test
    void updateMemberWithJoin() {
        disseminator.updateMemberState(member, new Gossip(GossipType.JOIN, member, 0));

        verify(memberList).add(member);
    }
}