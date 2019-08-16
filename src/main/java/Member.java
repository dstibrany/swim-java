import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Member {
    static final int BYTES = Integer.BYTES + Integer.BYTES;
    private int port;
    private InetAddress address;
    private Lock mutex = new ReentrantLock();

    Member(int port, InetAddress address) {
        this.address = address;
        this.port = port;
    }

    int getPort() {
        return port;
    }

    InetAddress getAddress() {
        return address;
    }

    Lock getMutex() {
        return mutex;
    }

    @Override
    public String toString() {
        return address.getHostAddress() + ":" + port;
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
