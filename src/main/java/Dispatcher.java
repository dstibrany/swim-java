import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Dispatcher {
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private TransportFactory tf;
    private Transport listener;

    Dispatcher(TransportFactory tf, Config conf) {
        this.tf = tf;
        listener = tf.createListener(conf.getPort());
    }

    Message receive() {
        return listener.receive();
    }

    void ack(Member member) throws InterruptedException, ExecutionException {
        Message ack = new Message(MessageType.ACK, member);
        Future<?> f = executor.submit(() -> {
            Transport t = tf.create();
            t.send(ack);
            t.close();
        });
        f.get();
    }

    void ping(Member member, int timeout) throws TimeoutException, InterruptedException, ExecutionException {
        Message ping = new Message(MessageType.PING, member);
        Future<Message> f = executor.submit(() -> {
            Transport t = tf.create();
            t.send(ping);
            Message ack = t.receive();
            t.close();
            return ack;
        });
        f.get(timeout, TimeUnit.MILLISECONDS);
    }


    void pingReq(List<Member> members, Member iProbeTarget, int timeout)
            throws TimeoutException, InterruptedException, ExecutionException {
        List<Callable<Message>> pingRequests = new ArrayList<>();
        for (Member member : members) {
            pingRequests.add(() -> {
                Transport t = tf.create();
                Message ping = new Message(MessageType.PING_REQ, member, iProbeTarget);
                t.send(ping);
                Message ack = t.receive();
                t.close();
                return ack;
            });
        }

        List<Future<Message>> acks = executor.invokeAll(pingRequests, timeout, TimeUnit.MILLISECONDS);

        int ackCount = 0;
        for (Future<Message> ack : acks) {
            if (!ack.isCancelled()) {
                ackCount++;
//                ackMessages.add(ack.get());
            }
        }

        if (ackCount == 0) throw new TimeoutException();
    }
}
