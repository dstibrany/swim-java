import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private MessageType messageType;
    private Member member;
    private Member indirectProbeMember;
    private List<Gossip> gossipList;

    Message(MessageType messageType, Member member, List<Gossip> gossipList) {
        this.messageType = messageType;
        this.member = member;
        this.gossipList = gossipList;
    }

    Message(MessageType messageType, Member member, Member indirectProbeMember, List<Gossip> gossipList) {
        this.messageType = messageType;
        this.member = member;
        this.indirectProbeMember = indirectProbeMember;
        this.gossipList = gossipList;
    }

    static Message deserialize(byte[] data, Member member) {
        MessageType messageType;
        Message message;

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            messageType = MessageType.getType(dis.readInt());
            Member iProbeMember = null;
            if (messageType == MessageType.PING_REQ) {
                byte[] address = new byte[Integer.BYTES];
                int bytesRead = dis.read(address);
                int port = dis.readInt();
                iProbeMember = new Member(port, InetAddress.getByAddress(address));
            }

            int numGossipMessages = dis.readInt();
            List<Gossip> gossipList = new ArrayList<>();
            for (int i = 0; i < numGossipMessages; i++) {
                byte[] gossipData = new byte[Gossip.BYTES];
                dis.read(gossipData);
                gossipList.add(Gossip.deserialize(gossipData));
            }

            if (messageType == MessageType.PING_REQ) {
                message = new Message(messageType, member, iProbeMember, gossipList);
            } else {
                message = new Message(messageType, member, gossipList);
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

    List<Gossip> getGossipList() {
        return gossipList;
    }

    byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(messageType.getValue());
            if (messageType == MessageType.PING_REQ) {
                dos.write(indirectProbeMember.getAddress().getAddress());
                dos.writeInt(indirectProbeMember.getPort());
            }
            dos.writeInt(gossipList.size());
            for (Gossip g : gossipList) {
                dos.write(g.serialize());
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
