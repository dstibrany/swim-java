package ca.davestibrany.swimjava;

import java.io.*;
import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Member {
    static final int BYTES = Integer.BYTES + Integer.BYTES + Integer.BYTES;
    private int port;
    private InetAddress address;
    private AtomicBoolean suspected = new AtomicBoolean(false);
    private AtomicInteger incarnationNumber;

    Member(int port, InetAddress address, int incarnationNumber) {
        this.address = address;
        this.port = port;
        this.incarnationNumber = new AtomicInteger(incarnationNumber);
    }

    Member(int port, InetAddress address) {
        this(port, address, 0);
    }

    static Member deserialize(byte[] data) {
        Member member;

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            byte[] address = new byte[Integer.BYTES];
            int bytesRead = dis.read(address);
            int port = dis.readInt();
            int incarnationNumber = dis.readInt();
            member = new Member(port, InetAddress.getByAddress(address), incarnationNumber);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }

        return member;
    }

    int getPort() {
        return port;
    }

    InetAddress getAddress() {
        return address;
    }

    void suspect() {
        suspected.set(true);
    }

    void alive() {
        suspected.set(false);
    }

    int getIncarnationNumber() {
        return incarnationNumber.get();
    }

    void setIncarnationNumber(int value) {
        incarnationNumber.set(value);
    }

    void incrementIncarnationNumber() {
        incarnationNumber.incrementAndGet();
    }

    byte[] serialize() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream dos = new DataOutputStream(baos)) {
            dos.write(address.getAddress());
            dos.writeInt(port);
            dos.writeInt(incarnationNumber.get());
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    @Override
    public String toString() {
        return "Member{" +
                "port=" + port +
                ", address=" + address.getHostAddress() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return port == member.port &&
                address.equals(member.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, address);
    }
}
