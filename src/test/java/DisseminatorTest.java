import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisseminatorTest {

    private Member m1;
    private Disseminator disseminator;

    @SuppressWarnings("unchecked")
    private List<Member> memberList = (List<Member>) mock(List.class);
    private Member member;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        m1 = new Member(5555, InetAddress.getLoopbackAddress());
        disseminator = new Disseminator(memberList);
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
        disseminator.updateMember(member, GossipType.ALIVE);

        verify(member).alive();
        verifyZeroInteractions(memberList);
    }

    @Test
    void updateMemberWithSuspect() {
        disseminator.updateMember(member, GossipType.SUSPECT);

        verify(member).suspect();
        verifyZeroInteractions(memberList);
    }

    @Test
    void updateMemberWithConfirm() {
        disseminator.updateMember(member, GossipType.CONFIRM);

        verify(memberList).remove(member);
    }

    @Test
    void updateMemberWithJoin() {
        disseminator.updateMember(member, GossipType.JOIN);

        verify(memberList).add(member);
    }
}