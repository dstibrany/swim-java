import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemberListTest {

    @BeforeEach
    void setUp() {
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