import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwimJava {
    private static Member self;

    static Member getSelf() {
        return self;
    }

    public static void main(String[] args) {
        Config conf = new Config();
        Dispatcher d = new Dispatcher(new TransportFactory(), conf);
        self = new Member(conf.getPort(), conf.getAddress());
        List<Member> memberlist = new ArrayList<>();
        memberlist.add(self);

        ExecutorService e = Executors.newFixedThreadPool(2);
        e.submit(() -> {
            FailureDetector fd = new FailureDetector(memberlist, d, conf);
            try {
                fd.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
        e.submit(() -> {
            Listener listener = new Listener(d, conf);
            try {
                listener.start();
            } catch (Exception ex2) {
                ex2.printStackTrace();
                System.exit(1);
            }
        });
    }
}
