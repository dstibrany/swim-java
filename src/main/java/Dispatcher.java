import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Dispatcher {
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private TransportFactory tf;
    private Transport listener;
    private Disseminator disseminator;

    Dispatcher(TransportFactory tf, Disseminator d, Config conf) {
        this.tf = tf;
        disseminator = d;
        listener = tf.createListener(conf.getPort());
    }

    Message receive() {
        Message message = listener.receive();
        disseminator.mergeGossip(message.getGossipList());
        return message;
    }

    void ping(Member member, int timeout) throws TimeoutException, InterruptedException, ExecutionException {
        Message ping = new Message(MessageType.PING, member, disseminator.generateGossip());
        Future<Message> f = executor.submit(() -> {
            Transport t = tf.create();
            t.send(ping);
            Message ack = t.receive();
            disseminator.mergeGossip(ack.getGossipList());
            t.close();
            return ack;
        });
        f.get(timeout, TimeUnit.MILLISECONDS);
    }

    void ack(Member member) throws InterruptedException, ExecutionException {
        Message ack = new Message(MessageType.ACK, member, disseminator.generateGossip());
        Future<?> f = executor.submit(() -> {
            Transport t = tf.create();
            t.send(ack);
            t.close();
        });
        f.get();
    }

    void pingReq(List<Member> members, Member iProbeTarget, int timeout) throws TimeoutException, InterruptedException {
        List<Callable<Message>> pingRequests = new ArrayList<>();
        List<Gossip> gossipList = disseminator.generateGossip();
        for (Member member : members) {
            pingRequests.add(() -> {
                Transport t = tf.create();
                Message ping = new Message(MessageType.PING_REQ, member, iProbeTarget, gossipList);
                t.send(ping);
                Message ack = t.receive();
                disseminator.mergeGossip(ack.getGossipList());
                t.close();
                return ack;
            });
        }

        List<Future<Message>> acks = executor.invokeAll(pingRequests, timeout, TimeUnit.MILLISECONDS);

        int ackCount = 0;
        for (Future<Message> ack : acks) {
            if (!ack.isCancelled()) {
                ackCount++;
            }
        }

        if (ackCount == 0) throw new TimeoutException();
    }
}
