package ca.davestibrany.swimjava;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Gossip implements Comparable<Gossip> {
    static final int BYTES = GossipType.BYTES + Member.BYTES;
    private final GossipType gossipType;
    private final Member member;
    private final int incarnationNumber;
    private AtomicInteger piggybackCount = new AtomicInteger(0);
    private AtomicBoolean expired = new AtomicBoolean(false);

    Gossip(GossipType gossipType, Member member) {
        this.gossipType = gossipType;
        this.member = member;
        this.incarnationNumber = member.getIncarnationNumber();
    }

    static Gossip deserialize(byte[] data) {
        GossipType gossipType;
        Gossip gossip;

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            gossipType = GossipType.getType(dis.readInt());
            byte[] memberBuffer = new byte[Member.BYTES];
            int bytesRead = dis.read(memberBuffer);
            gossip = new Gossip(gossipType, Member.deserialize(memberBuffer));
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
            dos.write(member.serialize());

            piggybackCount.incrementAndGet();

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

    int getIncarnationNumber() {
        return incarnationNumber;
    }

    boolean isExpired() {
        return expired.get();
    }

    void setExpired() {
        expired.set(true);
    }

    int getPiggyBackCount() {
        return piggybackCount.get();
    }

    @Override
    public int compareTo(Gossip o) {
        return piggybackCount.get() - o.getPiggyBackCount();
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

    @Override
    public String toString() {
        return "Gossip{" +
                "gossipType=" + gossipType +
                ", member=" + member +
                ", incarnationNumber=" + incarnationNumber +
                '}';
    }
}
