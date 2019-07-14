import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private MessageType messageType;
    private int srcPort;
    private InetAddress srcAddress;
    private int dstPort;
    private InetAddress dstAddress;

    public Message(MessageType messageType, int dstPort, InetAddress dstAddress) {
        this.messageType = messageType;
        this.dstPort = dstPort;
        this.dstAddress = dstAddress;
    }

    public Message(byte[] packetData, int srcPort, InetAddress srcAddress) {
        this.srcPort = srcPort;
        this.srcAddress = srcAddress;
        deserialize(packetData);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public InetAddress getSrcAddress() {
        return srcAddress;
    }

    public int getDstPort() {
        return dstPort;
    }

    public InetAddress getDstAddress() {
        return dstAddress;
    }

    public byte[] serialize()  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(messageType.getValue());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return baos.toByteArray();
    }

    public void deserialize(byte[] packetData) {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetData));
        try {
            messageType = MessageType.getType(dis.readInt());
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
