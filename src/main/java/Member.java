import java.net.InetAddress;

public class Member {
    private int port;
    private InetAddress address;

    Member(int port, InetAddress address) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address.getHostAddress() + ":" + port;
    }
}
