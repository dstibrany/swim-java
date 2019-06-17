import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class NetTransport implements Transport {
    private DatagramSocket socket;

    public NetTransport(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public Message receive() throws IOException {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
        socket.receive(packet);
        return new Message(packet.getData(), packet.getPort(), packet.getAddress());
    }

    @Override
    public void send(Message message) throws IOException {
        byte[] messageData = message.serialize();
        DatagramPacket packet = new DatagramPacket(messageData, 0, messageData.length, message.getDstAddress(), message.getDstPort());
        socket.send(packet);
    }
}
