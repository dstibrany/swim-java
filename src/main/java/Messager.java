import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Messager {
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Transport t;

    Message ping(Member member) throws TimeoutException, InterruptedException, ExecutionException  {
        Message ping = new Message(MessageType.PING, member);
        Future<Message> f = executor.submit(() -> {
            Transport t = new NetTransport();
            t.send(ping);
            Message ack = t.receive();
            t.close();
            return ack;
        });
        return f.get(1000, TimeUnit.MILLISECONDS);
    }

    void ack(Member member) {
        Message ack = new Message(MessageType.ACK, member);
        executor.submit(() -> {
            Transport t = new NetTransport();
            t.send(ack);
            t.close();
        });
    }

    List<Message> pingReq(List<Member> members) throws TimeoutException, InterruptedException, ExecutionException {
        List<Callable<Message>> messages = new ArrayList<>();
        for (Member member : members) {
            messages.add(() -> {
                Transport t = new NetTransport();
                Message ping = new Message(MessageType.PING, member);
                t.send(ping);
                Message ack = t.receive();
                t.close();
                return ack;
            });
        }
        List<Future<Message>> acks = executor.invokeAll(messages, 1000, TimeUnit.MILLISECONDS);
        List<Message> ackMessages = new ArrayList<>();
        for (Future<Message> ack : acks) {
            if (ack.isDone()) ackMessages.add(ack.get());
        }
        return ackMessages;
    }

    void indirectProbe(Member from, Member to) {
        Message ping = new Message(MessageType.PING, to);
        executor.submit(() -> {
            Transport t = new NetTransport();
            t.send(ping);
            Message ack = t.receive();
            t.send(new Message(MessageType.ACK, from));
            t.close();
        });
    }

}
