package ca.davestibrany.swimjava;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MemberListTest {
    Set<Member> members;
    private Member self;
    private MemberList memberList;

    @BeforeEach
    void setUp() {
        self = new Member(5555, InetAddress.getLoopbackAddress());
        members = new HashSet<>();
        memberList = new MemberList(members, self);
    }

    @Test
    void updateMemberStateOnJoin() {
        Member newMember = new Member(5556, InetAddress.getLoopbackAddress());
        Gossip g1 = new Gossip(GossipType.JOIN, newMember);

        memberList.updateMemberState(g1);

        assertEquals(2, members.size());
    }

    @Test
    void updateMemberStateOnConfirm() {
        Member memberToDelete = new Member(5556, InetAddress.getLoopbackAddress());
        members.add(memberToDelete);
        Gossip g1 = new Gossip(GossipType.CONFIRM, memberToDelete);

        memberList.updateMemberState(g1);

        assertFalse(members.contains(memberToDelete));
    }

    @Test
    void updateMemberStateOnAlive() {
        Member m = mock(Member.class);
        members.add(m);
        Gossip g1 = new Gossip(GossipType.ALIVE, m);

        memberList.updateMemberState(g1);

        verify(m).alive();
    }

    @Test
    void updateMemberStateOnSuspect() {
        Member m = mock(Member.class);
        members.add(m);
        Gossip g1 = new Gossip(GossipType.SUSPECT, m);

        memberList.updateMemberState(g1);

        verify(m).suspect();
    }

    @Test
    void testModifyIncarnationNumber() {
        int originalIncarnationNumber = 5;
        Member memberToModify = new Member(5556, InetAddress.getLoopbackAddress(), originalIncarnationNumber);
        Member memberInGossip = new Member(5556, InetAddress.getLoopbackAddress(), originalIncarnationNumber + 1);
        members.add(memberToModify);
        Gossip g1 = new Gossip(GossipType.ALIVE, memberInGossip);

        memberList.updateMemberState(g1);

        assertEquals(originalIncarnationNumber + 1, memberToModify.getIncarnationNumber());
    }

    @Test
    void testDoNotModifyIncarnationNumber() {
        int originalIncarnationNumber = 5;
        Member memberToModify = new Member(5556, InetAddress.getLoopbackAddress(), originalIncarnationNumber);
        Member memberInGossip = new Member(5556, InetAddress.getLoopbackAddress(), originalIncarnationNumber - 1);
        members.add(memberToModify);
        Gossip g1 = new Gossip(GossipType.ALIVE, memberInGossip);

        memberList.updateMemberState(g1);

        assertEquals(originalIncarnationNumber, memberToModify.getIncarnationNumber());
    }

    @Test
    void testGetRandomMembers() {
        int port = 1;
        int k = 5;
        Member m1 = new Member(++port, InetAddress.getLoopbackAddress());
        Member m2 = new Member(++port, InetAddress.getLoopbackAddress());
        Member m3 = new Member(++port, InetAddress.getLoopbackAddress());
        Member m4 = new Member(++port, InetAddress.getLoopbackAddress());
        Member m5 = new Member(++port, InetAddress.getLoopbackAddress());
        MemberList membershipList = new MemberList(new HashSet<>(Arrays.asList(m1, m2, m3, m4, m5)), null);
        int originalSize = membershipList.size();

        List<Member> randomMembers1 = membershipList.getRandomMembers(k, null);
        List<Member> randomMembers2 = membershipList.getRandomMembers(k, null);

        assertEquals(k, randomMembers1.size());
        assertEquals(k, randomMembers2.size());
        assertEquals(originalSize, membershipList.size());
        assertNotEquals(randomMembers1, randomMembers2);
    }

    @Test
    void testGetRandomMembersDoesNotReturnSelf() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member self = new Member(5555, InetAddress.getLoopbackAddress());
        MemberList memberList = new MemberList(self);
        memberList.add(m1);

        for (int i = 0; i < 100; i++) {
            List<Member> randomMembers = memberList.getRandomMembers(1, null);
            assertEquals(m1, randomMembers.get(0));
        }
    }

    @Test
    void testGetRandomMembersDoesNotReturnTargetToExclude() {
        Member self = new Member(5555, InetAddress.getLoopbackAddress());
        Member memberToExclude = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        MemberList memberList = new MemberList(self);
        memberList.add(memberToExclude);
        memberList.add(m2);

        for (int i = 0; i < 10; i++) {
            List<Member> randomMembers = memberList.getRandomMembers(1, memberToExclude);
            assertEquals(m2, randomMembers.get(0));
        }
    }

}