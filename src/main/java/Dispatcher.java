import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Dispatcher {
    private final int timeout = 5000;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private TransportFactory tf;
    private Transport listener;

    Dispatcher(TransportFactory tf) {
        this.tf = tf;
        listener = tf.createListener(5555);
    }

    Message receive() {
        return listener.receive();
    }

    Message ping(Member member) throws TimeoutException, InterruptedException, ExecutionException {
        Message ping = new Message(MessageType.PING, member);
        Future<Message> f = executor.submit(() -> {
            Transport t = tf.create();
            t.send(ping);
            Message ack = t.receive();
            t.close();
            return ack;
        });
        return f.get(timeout, TimeUnit.MILLISECONDS);
    }

    void ack(Member member) {
        Message ack = new Message(MessageType.ACK, member);
        executor.submit(() -> {
            Transport t = tf.create();
            t.send(ack);
            t.close();
        });
    }

    List<Message> pingReq(List<Member> members, Member iProbeTarget) throws TimeoutException, InterruptedException, ExecutionException {
        List<Callable<Message>> messages = new ArrayList<>();
        for (Member member : members) {
            messages.add(() -> {
                Transport t = tf.create();
                Message ping = new Message(MessageType.PING_REQ, member, iProbeTarget);
                t.send(ping);
                Message ack = t.receive();
                t.close();
                return ack;
            });
        }
        List<Future<Message>> acks = executor.invokeAll(messages, timeout, TimeUnit.MILLISECONDS);
        List<Message> ackMessages = new ArrayList<>();
        for (Future<Message> ack : acks) {
            if (!ack.isCancelled()) {
                ackMessages.add(ack.get());
            }
        }
        if (ackMessages.size() == 0) throw new TimeoutException();
        return ackMessages;
    }
}
