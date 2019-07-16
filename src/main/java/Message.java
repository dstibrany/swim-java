import java.io.*;
import java.net.InetAddress;

public class Message {
    private MessageType messageType;
    private Member member;
    private Member indirectProbeMember;

    Message(MessageType messageType, Member member) {
        this.messageType = messageType;
        this.member = member;
    }

    Message(MessageType messageType, Member member, Member indirectProbeMember) {
        this.messageType = messageType;
        this.member = member;
        this.indirectProbeMember = indirectProbeMember;
    }

    static Message deserialize(byte[] data, Member member) {
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

    MessageType getMessageType() {
        return messageType;
    }

    Member getMember() {
        return member;
    }

    Member getIndirectProbeMember() {
        return indirectProbeMember;
    }

    byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(messageType.getValue());
            if (messageType == MessageType.PING_REQ) {
                dos.write(indirectProbeMember.getAddress().getAddress());
                dos.writeInt(indirectProbeMember.getPort());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
