import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListenerTest {
    private Dispatcher dispatcher;
    private Listener listener;
    private InOrder inOrder;
    private  Member sender;
    private Member iProbeTarget;

    @BeforeEach
    void setUp() {
        dispatcher = mock(Dispatcher.class);
        listener = new Listener(dispatcher);
        inOrder = inOrder(dispatcher);
        sender = new Member(1234, InetAddress.getLoopbackAddress());
        iProbeTarget = new Member(1235, InetAddress.getLoopbackAddress());
    }

    @Test
    void testReceivedPing() throws InterruptedException, ExecutionException {
        Message ping = new Message(MessageType.PING, sender);
        when(dispatcher.receive()).thenReturn(ping);

        listener.listenerHandler();
        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ack(sender);

    }

    @Test
    void testReceivedPingReq() throws InterruptedException, ExecutionException, TimeoutException {
        Message pingReq = new Message(MessageType.PING_REQ, sender, iProbeTarget);
        when(dispatcher.receive()).thenReturn(pingReq);

        listener.listenerHandler();

        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ping(iProbeTarget);
        inOrder.verify(dispatcher).ack(sender);

    }

    @Test
    void testReceivedPingReqNoAck() throws InterruptedException, ExecutionException, TimeoutException {
        Message pingReq = new Message(MessageType.PING_REQ, sender, iProbeTarget);
        when(dispatcher.receive()).thenReturn(pingReq);
        when(dispatcher.ping(iProbeTarget)).thenThrow(new TimeoutException());

        listener.listenerHandler();

        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ping(iProbeTarget);
        verify(dispatcher, never()).ack(sender);
    }

}