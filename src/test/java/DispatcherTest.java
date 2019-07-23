import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DispatcherTest {

    private Dispatcher d;
    private Transport t;
    private InOrder inOrder;

    @BeforeEach
    void setUp() {
        t = mock(NetTransport.class);
        inOrder = inOrder(t);
        TransportFactory tfStub = new TransportFactory() {
            Transport create() {
                return t;
            }

            Transport createListener(int port) {
                return t;
            }
        };
        d = new Dispatcher(tfStub);
    }

    @Test
    void testReceive() {
        d.receive();
        verify(t).receive();
    }

    @Test
    void testPing() throws TimeoutException, InterruptedException, ExecutionException {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);

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
        ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
        when(t.receive()).thenAnswer(i -> {
            Thread.sleep(pingTimeout + 10);
            return new Message(MessageType.ACK, m2);
        });

        assertThrows(TimeoutException.class, () -> {
            d.ping(m1, pingTimeout);
        });
    }

    @Test
    void ack() {
    }

    @Test
    void pingReq() {
    }
}