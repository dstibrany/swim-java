import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailureDetectorTest {
    private Dispatcher dispatcher;
    private FailureDetector fd;
    private FailureDetector fdSpy;
    private InOrder inOrder;
    private List<Member> memberList;
    private List<Member> memberListSpy;
    private int reqTimeout;

    @BeforeEach
    void setUp() {
        int protocolPeriod = 10;
        reqTimeout = 10;

        dispatcher = mock(Dispatcher.class);
        memberList = new ArrayList<>();
        memberListSpy = spy(memberList);
        fd = new FailureDetector(memberList, dispatcher);
        fd.setReqTimeout(reqTimeout);
        fd.setProtocolPeriod(protocolPeriod);
        fdSpy = spy(fd);
        inOrder = inOrder(dispatcher);
    }

    @Test
    void testSendPingReceiveAck() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        memberList.add(SwimJava.getSelf());
        memberList.add(target);
        when(dispatcher.ping(target, reqTimeout)).thenReturn(new Message(MessageType.ACK, target));

        fd.runProtocol();
        verify(dispatcher).ping(target, reqTimeout);
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoAckThenIndirectProbe() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(SwimJava.getSelf());
        memberList.add(target);
        memberList.add(pingReqTarget);
        when(dispatcher.ping(target, reqTimeout)).thenThrow(TimeoutException.class);
        when(dispatcher.pingReq(Arrays.asList(pingReqTarget), target, reqTimeout)).thenReturn(new ArrayList<Message>());
        when(fdSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();
        inOrder.verify(dispatcher).ping(target, reqTimeout);
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, reqTimeout);
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoAckThenIndirectProbeTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(SwimJava.getSelf());
        memberList.add(target);
        memberList.add(pingReqTarget);
        when(dispatcher.ping(target, reqTimeout)).thenThrow(TimeoutException.class);
        when(dispatcher.pingReq(Arrays.asList(pingReqTarget), target, reqTimeout)).thenThrow(TimeoutException.class);
        when(fdSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();
        inOrder.verify(dispatcher).ping(target, reqTimeout);
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, reqTimeout);
        verify(memberListSpy).remove(any(Member.class));
        assertFalse(memberList.contains(target));
    }

    @Test
    void testGetRandomMembers() {
        int port = 1;
        int k = 5;
        Member m1 = new Member(port++, InetAddress.getLoopbackAddress());
        Member m2 = new Member(port++, InetAddress.getLoopbackAddress());
        Member m3 = new Member(port++, InetAddress.getLoopbackAddress());
        Member m4 = new Member(port++, InetAddress.getLoopbackAddress());
        Member m5 = new Member(port++, InetAddress.getLoopbackAddress());
        List<Member> membershiplist = List.of(m1, m2, m3, m4, m5);
        int originalSize = membershiplist.size();

        FailureDetector fd = new FailureDetector(membershiplist, dispatcher);
        List<Member> randomMembers1 = fd.getRandomMembers(k, null);
        List<Member> randomMembers2 = fd.getRandomMembers(k, null);
        assertEquals(k, randomMembers1.size());
        assertEquals(k, randomMembers2.size());
        assertTrue(membershiplist.size() == originalSize);
        assertNotEquals(randomMembers1, randomMembers2);
    }

    @Test
    void testGetRandomMembersDoesNotReturnSelf() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        List<Member> membershiplist = List.of(SwimJava.getSelf(), m1);

        FailureDetector fd = new FailureDetector(membershiplist, dispatcher);
        for (int i = 0; i < 100; i++) {
            List<Member> randomMembers = fd.getRandomMembers(1, null);
            assertEquals(m1, randomMembers.get(0));
        }
    }

    @Test
    void testGetRandomMembersDoesNotReturnTargetToExclude() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        List<Member> membershiplist = List.of(SwimJava.getSelf(), m1, m2);

        FailureDetector fd = new FailureDetector(membershiplist, dispatcher);
        for (int i = 0; i < 100; i++) {
            List<Member> randomMembers = fd.getRandomMembers(1, m1);
            assertEquals(m2, randomMembers.get(0));
        }
    }

    @Test
    void testNoMembersToPing() throws InterruptedException, ExecutionException, TimeoutException {
        memberList.add(SwimJava.getSelf());
        fd.runProtocol();
        verify(dispatcher, never()).ping(any(Member.class), anyInt());
        verify(dispatcher, never()).pingReq(any(List.class), any(Member.class), anyInt());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoMembersToPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        when(dispatcher.ping(target, reqTimeout)).thenThrow(TimeoutException.class);
        memberList.add(SwimJava.getSelf());
        memberList.add(target);

        fd.runProtocol();

        verify(dispatcher).ping(target, reqTimeout);
        verify(dispatcher, never()).pingReq(any(List.class), any(Member.class), anyInt());
        verify(memberListSpy).remove(target);
    }
}