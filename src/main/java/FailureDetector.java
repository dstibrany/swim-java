import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class FailureDetector {
    private int protocolPeriod = 5;
    private int subgroupSize = 2;
    private List<Member> membershipList;
    private Dispatcher dispatcher;
    private final Logger logger;


    FailureDetector(List<Member> membershipList, Dispatcher dispatcher) {
        this.membershipList = membershipList;
        this.dispatcher = dispatcher;
        logger = LogManager.getFormatterLogger();
    }

    public void start() throws InterruptedException, ExecutionException {
        while (true) {
            Member target = membershipList.get(0);
            try {
                logger.info("Sending PING to %s", target.toString());
                Message ack = dispatcher.ping(target);
                logger.info("Received ACK from %s", target.toString());
            } catch (TimeoutException e) {
                logger.info("Timeout while waiting for ACK from %s", target.toString());
                List<Member> targets = Arrays.asList(membershipList.get(0));
                try {
                    logger.info("Sending PING-REQ to %d hosts", targets.size());
                    List<Message> messages = dispatcher.pingReq(targets);
                } catch (TimeoutException e2) {
                    logger.info("No ACKs from Indirect Probes, dropping %s from membership list", target.toString());
                    membershipList.remove(target);
                }
            }
            Thread.sleep(1000);
            logger.info("%s is still alive", target.toString());
        }
    }
}
