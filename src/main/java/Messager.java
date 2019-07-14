import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;

public class Messager {
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Transport t;

    Messager() {

    }

    Message ping(Member m) throws TimeoutException, InterruptedException, ExecutionException  {
        Message ping = new Message(MessageType.PING, m.getPort(), m.getAddress());
        Future<Message> f = executor.submit(() -> {
            Transport t = new NetTransport();
            t.send(ping, m);
            Message ack = t.receive();
            t.close();
            return ack;
        });
        return f.get(1000, TimeUnit.MILLISECONDS);
    }

    void ack (Member m) {
        Message ack = new Message(MessageType.ACK, m.getPort(), m.getAddress());
        executor.submit(() -> {
            Transport t = new NetTransport();
            t.send(ack, m);
            t.close();
        });
    }

    List<Message> indirectProbe(List<Member> members) throws TimeoutException {
//       executor.invokeAny();
        return null;
    }

    Message pingReq() {
        return null;
    }

}
