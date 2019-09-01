import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
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
    private MemberList memberList;
    private MemberList memberListSpy;
    private Config config;
    private Disseminator disseminator;

    @BeforeEach
    void setUp() {
        com.typesafe.config.Config mergedConf = ConfigFactory
                .parseString("swim-java.protocol_period=10, swim-java.request_timeout=10")
                .withFallback(ConfigFactory.defaultReference());
        config = new Config(mergedConf);
        dispatcher = mock(Dispatcher.class);
        disseminator = mock(Disseminator.class);
        memberList = new MemberList(config.getSelf());
        memberListSpy = spy(memberList);
        fd = new FailureDetector(memberListSpy, dispatcher, disseminator, config);
        fdSpy = spy(fd);
        inOrder = inOrder(dispatcher, disseminator);
    }

    @Test
    void testSendPingReceiveAck() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());

        memberList.add(target);

        fd.runProtocol();
        verify(dispatcher).ping(target, config.getReqTimeout());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoAckThenIndirectProbe() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(target);
        memberList.add(pingReqTarget);
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        when(memberListSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();
        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoAckThenIndirectProbeTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(target);
        memberList.add(pingReqTarget);
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        doThrow(TimeoutException.class).when(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        when(memberListSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();

        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        inOrder.verify(disseminator).suspect(target);
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
        MemberList membershipList = new MemberList(new HashSet<>(Arrays.asList(m1, m2, m3, m4, m5)), null);
        int originalSize = membershipList.size();

        FailureDetector fd = new FailureDetector(membershipList, dispatcher, disseminator, config);
        List<Member> randomMembers1 = membershipList.getRandomMembers(k, null);
        List<Member> randomMembers2 = membershipList.getRandomMembers(k, null);
        assertEquals(k, randomMembers1.size());
        assertEquals(k, randomMembers2.size());
        assertTrue(membershipList.size() == originalSize);
        assertNotEquals(randomMembers1, randomMembers2);
    }

    @Test
    void testGetRandomMembersDoesNotReturnSelf() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        MemberList memberList = new MemberList(config.getSelf());
        memberList.add(m1);

        FailureDetector fd = new FailureDetector(memberList, dispatcher, disseminator, config);
        for (int i = 0; i < 100; i++) {
            List<Member> randomMembers = memberList.getRandomMembers(1, null);
            assertEquals(m1, randomMembers.get(0));
        }
    }

    @Test
    void testGetRandomMembersDoesNotReturnTargetToExclude() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        MemberList memberList = new MemberList(config.getSelf());
        memberList.add(m1);
        memberList.add(m2);

        FailureDetector fd = new FailureDetector(memberList, dispatcher, disseminator, config);
        for (int i = 0; i < 10; i++) {
            List<Member> randomMembers = memberList.getRandomMembers(1, m1);
            assertEquals(m2, randomMembers.get(0));
        }
    }

    @Test
    void testNoMembersToPing() throws InterruptedException, ExecutionException, TimeoutException {
        fd.runProtocol();
        verify(dispatcher, never()).ping(any(Member.class), anyInt());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoMembersToPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        memberList.add(target);

        fd.runProtocol();

        verify(dispatcher).ping(target, config.getReqTimeout());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        // TODO: add this back
//        verify(memberListSpy).remove(target);
    }
}