import com.typesafe.config.ConfigFactory;
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
    private Config config;

    @BeforeEach
    void setUp() {
        com.typesafe.config.Config mergedConf = ConfigFactory
                .parseString("swim-java.protocol_period=10, swim-java.request_timeout=10")
                .withFallback(ConfigFactory.defaultReference());
        config = new Config(mergedConf);
        dispatcher = mock(Dispatcher.class);
        listener = new Listener(dispatcher, config);
        inOrder = inOrder(dispatcher);
        sender = new Member(1234, InetAddress.getLoopbackAddress());
        iProbeTarget = new Member(1235, InetAddress.getLoopbackAddress());
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
        inOrder.verify(dispatcher).ping(iProbeTarget, config.getReqTimeout());
        inOrder.verify(dispatcher).ack(sender);
    }

    @Test
    void testReceivedPingReqNoAck() throws InterruptedException, ExecutionException, TimeoutException {
        Message pingReq = new Message(MessageType.PING_REQ, sender, iProbeTarget);
        when(dispatcher.receive()).thenReturn(pingReq);
        doThrow(TimeoutException.class).when(dispatcher).ping(iProbeTarget, config.getReqTimeout());

        listener.listenerProtocol();

        inOrder.verify(dispatcher).receive();
        inOrder.verify(dispatcher).ping(iProbeTarget, config.getReqTimeout());
        verify(dispatcher, never()).ack(sender);
    }

}