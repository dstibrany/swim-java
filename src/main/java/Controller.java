import java.io.IOException;
import java.net.SocketException;

public class Controller {

    public static void main(String[] args) throws SocketException, IOException {
        FailureDetector fDetector = new FailureDetector();
        Listener listener = new Listener(new NetTransport(5555));
        fDetector.start();
        listener.start();
    }
}
