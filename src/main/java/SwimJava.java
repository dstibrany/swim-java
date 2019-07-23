import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwimJava {
    private static int selfPort = 5555;
    private static InetAddress selfAddress = InetAddress.getLoopbackAddress();
    private static Member self = new Member(selfPort, selfAddress);

    static Member getSelf() {
        return self;
    }

    public static void main(String[] args) {
        Dispatcher d = new Dispatcher(new TransportFactory());
        List<Member> memberlist = new ArrayList<>();
        memberlist.add(new Member(5556, InetAddress.getLoopbackAddress()));
        memberlist.add(self);

        ExecutorService e = Executors.newFixedThreadPool(2);
        e.submit(() -> {
            FailureDetector fd = new FailureDetector(memberlist, d);
            try {
                fd.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
        e.submit(() -> {
            Listener listener = new Listener(d);
            try {
                listener.start();
            } catch (Exception ex2) {
                ex2.printStackTrace();
                System.exit(1);
            }
        });
    }
}
