import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Net {

    public void run(int portNumber) throws IOException {
        byte[] buf = new byte[256];
        DatagramSocket dSocket = new DatagramSocket(portNumber);

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buf, 0, buf.length);
            dSocket.receive(inPacket);
            Message inMessage = new Message(inPacket);
            int inPort = inMessage.getPort();
            InetAddress inAddress = inMessage.getAddress();

            switch (inMessage.getMessageType()) {
                case PING:
                    System.out.printf("Received PING from %s : %d\n", inAddress.getHostAddress(), inPort);
                    Message ack = new Message(Message.MessageType.ACK);
                    byte[] ackData = ack.serialize();
                    DatagramPacket outPacket = new DatagramPacket(ackData, 0, ackData.length, inAddress, inPort);
                    dSocket.send(outPacket);
                    System.out.printf("Sent ACK from %s : %d\n", inAddress.getHostAddress(), inPort);
                    break;
                case PING_REQ:
                    System.out.println("Received PING-REQ");
                    break;
                case ACK:
                    System.out.println("Received ACK");
                    break;
                default:
                    System.out.println("Unknown message type");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Net n = new Net();
        new Thread(() -> {
            try {
                n.run(5555);
            } catch (IOException e) {
                System.out.println(e);
            }
        }).start();

        /*
        while (1) {
            receive
            if (Ping)
              sendAck();

            else if (PING REQ)
              sendPing()
              ReceiveAck()
              SendAck();
            else if (ACK)
              SencAck()

        }
         */
    }
}
