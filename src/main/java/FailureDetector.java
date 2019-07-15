import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class FailureDetector {
    private int protocolPeriod = 5;
    private int subgroupSize = 2;
    private List<Member> membershipList;
    private Transport t;
    private Messager messager = new Messager();


    FailureDetector(List<Member> membershipList, Transport t) {
        this.membershipList = membershipList;
        this.t = t;
    }

    public void start() throws InterruptedException, ExecutionException {
        while (true) {
            Member target = membershipList.get(0);
            try {
                Message ack = messager.ping(target);
            } catch (TimeoutException e) {
                List<Member> targets = Arrays.asList(membershipList.get(0));
                try {
                    List<Message> messages = messager.pingReq(targets);
                } catch (TimeoutException e2) {
                    membershipList.remove(target);
                }
            }
            Thread.sleep(1000);
        }
    }
}
