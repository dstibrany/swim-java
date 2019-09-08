import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {
    static final int BYTES = MessageType.BYTES + Member.BYTES + Integer.BYTES + (Gossip.BYTES * 6); // TODO: fix hardcoded 6
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
                byte[] memberBuffer = new byte[Member.BYTES];
                int bytesRead = dis.read(memberBuffer);
                iProbeMember = Member.deserialize(memberBuffer);
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

    byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(messageType.getValue());
            if (messageType == MessageType.PING_REQ) {
                dos.write(indirectProbeMember.serialize());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageType == message.messageType &&
                member.equals(message.member) &&
                Objects.equals(indirectProbeMember, message.indirectProbeMember) &&
                gossipList.equals(message.gossipList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageType, member, indirectProbeMember, gossipList);
    }

}
