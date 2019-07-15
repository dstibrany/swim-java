import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SwimJava {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Dispatcher m = new Dispatcher(new NetTransportFactory());
        List<Member> memberlist = new ArrayList<>();
        memberlist.add(new Member(5555, InetAddress.getLoopbackAddress()));

        FailureDetector fd = new FailureDetector(memberlist, m);
        fd.start();
//        Listener listener = new Listener(m);
//        listener.start();
    }

}
