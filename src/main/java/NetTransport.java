import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NetTransport implements Transport {
    private DatagramSocket socket;

    public NetTransport(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public NetTransport() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Message receive() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Member member = new Member(packet.getPort(), packet.getAddress());
        return Message.deserialize(packet.getData(), member);
    }

    @Override
    public void send(Message message) {
        byte[] messageData = message.serialize();
        Member member = message.getMember();
        DatagramPacket packet = new DatagramPacket(messageData, 0, messageData.length, member.getAddress(), member.getPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAndReceive(Message message) {

    }

    @Override
    public void close() {
        socket.close();
    }
}
