import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatcherTest {

    private Dispatcher d;
    private Transport t;
    private InOrder inOrder;
    private Config conf;
    private Disseminator disseminator;
    private List<Gossip> gossipList;

    @BeforeEach
    void setUp() {
        conf = new Config();
        t = mock(NetTransport.class);
        disseminator = mock(Disseminator.class);
        inOrder = inOrder(t);
        TransportFactory tfStub = new TransportFactory() {
            Transport create() {
                return t;
            }

            Transport createListener(int port) {
                return t;
            }
        };
        d = new Dispatcher(tfStub, disseminator, conf);
        gossipList = new ArrayList<>();
    }

    @Test
    void testReceive() {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        when(t.receive()).thenReturn(new Message(MessageType.PING, m1, gossipList));
        d.receive();
        verify(t).receive();
    }

    @Test
    void testPing() throws TimeoutException, InterruptedException, ExecutionException {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        when(t.receive()).thenReturn(new Message(MessageType.ACK, m1, gossipList));

        d.ping(m1, 100);

        inOrder.verify(t).send(argument.capture());
        inOrder.verify(t).receive();
        inOrder.verify(t).close();
        assertEquals(m1, argument.getValue().getMember());
        assertEquals(MessageType.PING, argument.getValue().getMessageType());
    }

    @Test
    void testPingWithTimeout() {
        int pingTimeout = 10;
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        when(t.receive()).thenAnswer(i -> {
            Thread.sleep(pingTimeout + 10);
            return new Message(MessageType.ACK, m2, gossipList);
        });

        assertThrows(TimeoutException.class, () -> {
            d.ping(m1, pingTimeout);
        });
    }

    @Test
    void testPingReq() throws TimeoutException, InterruptedException {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member iProbeTarget = new Member(1236, InetAddress.getLoopbackAddress());
        List<Member> pingReqTargets = Arrays.asList(m1, m2);
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        when(t.receive()).thenReturn(
                new Message(MessageType.ACK, m1, gossipList),
                new Message(MessageType.ACK, m2, gossipList)
        );

        d.pingReq(pingReqTargets, iProbeTarget, 100);

        verify(t, times(pingReqTargets.size())).send(argument.capture());
        verify(t, times(pingReqTargets.size())).receive();
        verify(t, times(pingReqTargets.size())).close();
        List<Message> arguments = argument.getAllValues();
        List<Member> pingReqTargetsClone = new ArrayList<>(pingReqTargets);
        for (Message msg : arguments) {
            assertEquals(iProbeTarget, msg.getIndirectProbeMember());
            assertEquals(MessageType.PING_REQ, msg.getMessageType());
            assertTrue(pingReqTargetsClone.contains(msg.getMember()));
            pingReqTargetsClone.remove(msg.getMember());
        }
    }

    @Test
    void testPingReqWithTimeout() {
        int pingReqTimeout = 10;
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Member m2 = new Member(1235, InetAddress.getLoopbackAddress());
        Member iProbeTarget = new Member(1236, InetAddress.getLoopbackAddress());
        List<Member> pingReqTargets = Arrays.asList(m1, m2);
        when(t.receive()).thenAnswer(i -> {
            Thread.sleep(pingReqTimeout + 10);
            return new Message(MessageType.ACK, m2, gossipList);
        });

        assertThrows(TimeoutException.class, () -> {
            d.pingReq(pingReqTargets, iProbeTarget, pingReqTimeout);
        });

        verify(t, times(pingReqTargets.size())).send(any(Message.class));
    }

    @Test
    void testAck() throws Exception {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);

        d.ack(m1);

        inOrder.verify(t).send(argument.capture());
        inOrder.verify(t).close();
        assertEquals(m1, argument.getValue().getMember());
        assertEquals(MessageType.ACK, argument.getValue().getMessageType());
    }
}