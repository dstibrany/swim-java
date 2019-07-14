import java.net.InetAddress;

public class Member {
    private int port;
    private InetAddress address;

    Member(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }
}
