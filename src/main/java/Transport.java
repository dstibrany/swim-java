import java.io.IOException;

public interface Transport {
    Message receive() throws IOException;
    void send(Message message) throws IOException;
}
