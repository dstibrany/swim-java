import java.io.*;
import java.net.InetAddress;

public class Message {
    private MessageType messageType;
    private Member member;
    private Member indirectProbeMember;

    public Message(MessageType messageType, Member member) {
        this.messageType = messageType;
        this.member = member;
    }

    public Message(MessageType messageType, Member member, Member indirectProbeMember) {
        this.messageType = messageType;
        this.member = member;
        this.indirectProbeMember = indirectProbeMember;
    }

    public static Message deserialize(byte[] data, Member member) {
        MessageType messageType;
        Message message;

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            messageType = MessageType.getType(dis.readInt());
            if (messageType == MessageType.PING_REQ) {
                byte[] address = new byte[4];
                int bytesRead = dis.read(address);
                int port = dis.readInt();
                message = new Message(messageType, member, new Member(port, InetAddress.getByAddress(address)));
            } else {
                message = new Message(messageType, member);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        return message;
    }

    public MessageType getMessageType() {
        if (messageType == null) return MessageType.UNKNOWN;
        else return messageType;
    }

    public Member getMember() {
        return member;
    }

    public Member getIndirectProbeMember() {
        return indirectProbeMember;
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(messageType.getValue());
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
