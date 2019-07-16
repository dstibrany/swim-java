import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwimJava {

    public static void main(String[] args) {
        Dispatcher m = new Dispatcher(new NetTransportFactory());
        List<Member> memberlist = new ArrayList<>();
        memberlist.add(new Member(5556, InetAddress.getLoopbackAddress()));
        ExecutorService e = Executors.newFixedThreadPool(2);
        e.submit(() -> {
            FailureDetector fd = new FailureDetector(memberlist, m);
            try {
                fd.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
        e.submit(() -> {
            Listener listener = new Listener(m);
            try {
                listener.start();
            } catch (Exception ex2) {
                ex2.printStackTrace();
                System.exit(1);
            }
        });



    }

}
