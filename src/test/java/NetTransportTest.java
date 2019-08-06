import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NetTransportTest {

    private NetTransport transport;
    private DatagramSocket socket;
    private List<Gossip> gossipList;

    @BeforeEach
    void setUp() {
        socket = mock(DatagramSocket.class);
        transport = new NetTransport(socket);
        gossipList = new ArrayList<>();
    }

    @Test
    void testReceive() throws IOException {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Message ping = new Message(MessageType.PING, m1, gossipList);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((DatagramPacket) args[0]).setData(ping.serialize());
            ((DatagramPacket) args[0]).setAddress(m1.getAddress());
            ((DatagramPacket) args[0]).setPort(m1.getPort());
            return null;
        }).when(socket).receive(any(DatagramPacket.class));

        Message receivedMsg = transport.receive();
        assertEquals(MessageType.PING, receivedMsg.getMessageType());
        assertEquals(m1, receivedMsg.getMember());
    }

    @Test
    void testSend() throws IOException {
        Member m1 = new Member(1234, InetAddress.getLoopbackAddress());
        Message ping = new Message(MessageType.PING, m1, gossipList);
        ArgumentCaptor<DatagramPacket> argument = ArgumentCaptor.forClass(DatagramPacket.class);

        transport.send(ping);

        verify(socket).send(argument.capture());
        assertEquals(m1.getAddress(), argument.getValue().getAddress());
        assertEquals(m1.getPort(), argument.getValue().getPort());
        assertArrayEquals(ping.serialize(), argument.getValue().getData());
    }

    @Test
    void testClose() {
        transport.close();
        verify(socket).close();
    }
}