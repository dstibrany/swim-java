import java.io.*;
import java.net.InetAddress;
import java.util.Objects;

public class Gossip {
    static final int BYTES = Integer.BYTES + GossipType.BYTES + Member.BYTES;
    private GossipType gossipType;
    private Member member;
    private int incarnationNumber;

    Gossip(GossipType gossipType, Member member, int incarnationNumber) {
        this.gossipType = gossipType;
        this.member = member;
        this.incarnationNumber = incarnationNumber;
    }

    static Gossip deserialize(byte[] data) {
        GossipType gossipType;
        Gossip gossip;

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            gossipType = GossipType.getType(dis.readInt());
            byte[] address = new byte[Integer.BYTES];
            int bytesRead = dis.read(address);
            int port = dis.readInt();
            int incarnationNumber = dis.readInt();
            gossip = new Gossip(
                    gossipType,
                    new Member(port, InetAddress.getByAddress(address)),
                    incarnationNumber
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        return gossip;
    }

    byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(gossipType.getValue());
            dos.write(member.getAddress().getAddress());
            dos.writeInt(member.getPort());
            dos.writeInt(incarnationNumber);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    GossipType getGossipType() {
        return gossipType;
    }

    Member getMember() {
        return member;
    }

    boolean overrides(Gossip other) {
        return other == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gossip gossip = (Gossip) o;
        return incarnationNumber == gossip.incarnationNumber &&
                gossipType == gossip.gossipType &&
                member.equals(gossip.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gossipType, member, incarnationNumber);
    }
}
