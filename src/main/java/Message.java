import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Message {
    public enum MessageType {
        PING(1),
        PING_REQ(2),
        ACK(3);

        private final int value;
        private static final Map<Integer, MessageType> lookup = new HashMap<>();

        static {
            for (MessageType type : MessageType.values()) {
                lookup.put(type.getValue(), type);
            }
        }

        MessageType(int value) {
            this.value = value;
        }

        public static MessageType getType(int value) {
            return lookup.get(value);
        }

        public int getValue() {
            return value;
        }
    }

    private MessageType messageType;
    private int port;
    private InetAddress address;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(byte[] packetData, int port, InetAddress address) throws IOException {
        this.port = port;
        this.address = address;
        deserialize(packetData);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(messageType.getValue());
        return baos.toByteArray();
    }

    public void deserialize(byte[] packetData) throws IOException  {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetData));
        messageType = MessageType.getType(dis.readInt());
        dis.close();
    }

}
