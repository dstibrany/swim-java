import com.typesafe.config.ConfigFactory;
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
    private Config config;

    @BeforeEach
    void setUp() {
        com.typesafe.config.Config mergedConf = ConfigFactory
                .parseString("swim-java.protocol_period=10, swim-java.request_timeout=10")
                .withFallback(ConfigFactory.defaultReference());
        config = new Config(mergedConf);
        dispatcher = mock(Dispatcher.class);
        memberList = new ArrayList<>();
        memberListSpy = spy(memberList);
        fd = new FailureDetector(memberList, dispatcher, config);
        fdSpy = spy(fd);
        inOrder = inOrder(dispatcher);
    }

    @Test
    void testSendPingReceiveAck() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());

        memberList.add(SwimJava.getSelf());
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
        memberList.add(SwimJava.getSelf());
        memberList.add(target);
        memberList.add(pingReqTarget);
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        when(fdSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();
        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoAckThenIndirectProbeTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(SwimJava.getSelf());
        memberList.add(target);
        memberList.add(pingReqTarget);
        System.out.println(config.getReqTimeout());
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        when(fdSpy.getRandomMembers(1, null)).thenReturn(Arrays.asList(target));

        fdSpy.runProtocol();
        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        // TODO: add this back
//        verify(memberListSpy).remove(any(Member.class));
//        assertFalse(memberList.contains(target));
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
        List<Member> membershipList = Arrays.asList(m1, m2, m3, m4, m5);
        int originalSize = membershipList.size();

        FailureDetector fd = new FailureDetector(membershipList, dispatcher, config);
        List<Member> randomMembers1 = fd.getRandomMembers(k, null);
        List<Member> randomMembers2 = fd.getRandomMembers(k, null);
        assertEquals(k, randomMembers1.size());
        assertEquals(k, randomMembers2.size());
        assertTrue(membershipList.size() == originalSize);
        assertNotEquals(randomMembers1, randomMembers2);
    }

    @Test
    void testGetRandomMembersDoesNotReturnSelf() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        List<Member> membershiplist = Arrays.asList(SwimJava.getSelf(), m1);

        FailureDetector fd = new FailureDetector(membershiplist, dispatcher, config);
        for (int i = 0; i < 100; i++) {
            List<Member> randomMembers = fd.getRandomMembers(1, null);
            assertEquals(m1, randomMembers.get(0));
        }
    }

    @Test
    void testGetRandomMembersDoesNotReturnTargetToExclude() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        List<Member> membershipList = Arrays.asList(SwimJava.getSelf(), m1, m2);

        FailureDetector fd = new FailureDetector(membershipList, dispatcher, config);
        for (int i = 0; i < 10; i++) {
            List<Member> randomMembers = fd.getRandomMembers(1, m1);
            assertEquals(m2, randomMembers.get(0));
        }
    }

    @Test
    void testNoMembersToPing() throws InterruptedException, ExecutionException, TimeoutException {
        memberList.add(SwimJava.getSelf());
        fd.runProtocol();
        verify(dispatcher, never()).ping(any(Member.class), anyInt());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(memberListSpy, never()).remove(any(Member.class));
    }

    @Test
    void testNoMembersToPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        memberList.add(SwimJava.getSelf());
        memberList.add(target);

        fd.runProtocol();

        verify(dispatcher).ping(target, config.getReqTimeout());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        // TODO: add this back
//        verify(memberListSpy).remove(target);
    }
}