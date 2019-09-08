import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Test
    void checkBytes() {
        Member member = new Member(5555, InetAddress.getLoopbackAddress());
        assertEquals(Member.BYTES, member.serialize().length);
    }

    @Test
    void testSerialize() {
        Member member = new Member(5555, InetAddress.getLoopbackAddress());
        byte[] bytes = member.serialize();
        assertEquals(member, Member.deserialize(bytes));
    }

    @Test
    void testIncarnationNumber() {
        Member member = new Member(5555, InetAddress.getLoopbackAddress());

        member.incrementIncarnationNumber();
        assertEquals(1, member.getIncarnationNumber());

        member.setIncarnationNumber(10);
        assertEquals(10, member.getIncarnationNumber());
    }

}