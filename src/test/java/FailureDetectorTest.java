import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailureDetectorTest {
    private Dispatcher dispatcher;
    private FailureDetector fd;
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
        inOrder = inOrder(dispatcher, disseminator);
    }

    @Test
    void testSendPingReceiveAck() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());

        memberList.add(target);

        fd.runProtocol();

        verify(dispatcher).ping(target, config.getReqTimeout());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(disseminator, never()).suspect(target);
    }

    @Test
    void testNoAckThenIndirectProbe() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        Member pingReqTarget = new Member(1235, InetAddress.getLoopbackAddress());
        memberList.add(target);
        memberList.add(pingReqTarget);
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        when(memberListSpy.getRandomMembers(1, null)).thenReturn(Collections.singletonList(target));

        fd.runProtocol();

        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Collections.singletonList(pingReqTarget), target, config.getReqTimeout());
        verify(disseminator, never()).suspect(target);
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

        fd.runProtocol();

        inOrder.verify(dispatcher).ping(target, config.getReqTimeout());
        inOrder.verify(dispatcher).pingReq(Arrays.asList(pingReqTarget), target, config.getReqTimeout());
        inOrder.verify(disseminator).suspect(target);
    }


    @Test
    void testNoMembersToPing() throws InterruptedException, ExecutionException, TimeoutException {
        fd.runProtocol();

        verify(dispatcher, never()).ping(any(Member.class), anyInt());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(disseminator, never()).suspect(any(Member.class));
    }

    @Test
    void testNoMembersToPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Member target = new Member(1234, InetAddress.getLoopbackAddress());
        doThrow(TimeoutException.class).when(dispatcher).ping(target, config.getReqTimeout());
        memberList.add(target);

        fd.runProtocol();

        verify(dispatcher).ping(target, config.getReqTimeout());
        verify(dispatcher, never()).pingReq(anyList(), any(Member.class), anyInt());
        verify(disseminator).suspect(target);
    }
}