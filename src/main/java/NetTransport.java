import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetTransport implements Transport {
    private DatagramSocket socket;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public NetTransport(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public NetTransport()  {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Message receive()  {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Message(packet.getData(), packet.getPort(), packet.getAddress());
    }

    @Override
    public void send(Message message, Member member)  {
        byte[] messageData = message.serialize();
        DatagramPacket packet = new DatagramPacket(messageData, 0, messageData.length, message.getDstAddress(), message.getDstPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        socket.close();
    }
}
