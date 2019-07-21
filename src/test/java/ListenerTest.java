import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

class ListenerTest {
    private Dispatcher dispatcher;
    private Listener listener;
    private InOrder inOrder;
    private Member sender;
    private Member iProbeTarget;
    private int reqTimeout;

    @BeforeEach
    void setUp() {
        dispatcher = mock(Dispatcher.class);
        listener = new Listener(dispatcher);
        inOrder = inOrder(dispatcher);
        sender = new Member(1234, InetAddress.getLoopbackAddress());
        iProbeTarget = new Member(1235, InetAddress.getLoopbackAddress());
        reqTimeout = 1000;
        listener.setReqTimeout(reqTimeout);
    }

    @Test
    void testReceivedPing() throws InterruptedException, ExecutionException {
        Message ping = new Message(MessageType.PING, sender);
        when(dispatcher.receive()).thenReturn(ping);

        listener.listenerProtocol();
        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ack(sender);

    }

    @Test
    void testReceivedPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Message pingReq = new Message(MessageType.PING_REQ, sender, iProbeTarget);
        when(dispatcher.receive()).thenReturn(pingReq);

        listener.listenerProtocol();

        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ping(iProbeTarget, reqTimeout);
        inOrder.verify(dispatcher).ack(sender);

    }


    @Test
    void testReceivedPingReqNoAck() throws InterruptedException, ExecutionException, TimeoutException {
        Message pingReq = new Message(MessageType.PING_REQ, sender, iProbeTarget);
        when(dispatcher.receive()).thenReturn(pingReq);
        when(dispatcher.ping(iProbeTarget, reqTimeout)).thenThrow(new TimeoutException());

        listener.listenerProtocol();

        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ping(iProbeTarget, reqTimeout);
        verify(dispatcher, never()).ack(sender);
    }

}