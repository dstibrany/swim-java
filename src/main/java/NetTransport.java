import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

class NetTransport implements Transport {
    private DatagramSocket socket;

    NetTransport(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    NetTransport() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    NetTransport(DatagramSocket s) {
        socket = s;
    }

    @Override
    public Message receive() {
        byte[] buf = new byte[Message.BYTES];
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Message.deserialize(packet.getData(), new Member(packet.getPort(), packet.getAddress()));
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

    @Override
    public void close() {
        socket.close();
    }
}
