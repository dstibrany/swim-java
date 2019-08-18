import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Member {
    static final int BYTES = Integer.BYTES + Integer.BYTES;
    private int port;
    private InetAddress address;
    private AtomicBoolean suspected = new AtomicBoolean(false);

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

    void suspect() {
        suspected.set(true);
    }

    void alive() {
        suspected.set(false);
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
