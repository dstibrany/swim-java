import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class FailureDetector {
    private final Logger logger;
    private int protocolPeriod = 2000;
    private int subgroupSize = 2;
    private List<Member> membershipList;
    private Dispatcher dispatcher;
    private int reqTimeout = 1000;

    FailureDetector(List<Member> membershipList, Dispatcher dispatcher) {
        this.membershipList = membershipList;
        this.dispatcher = dispatcher;
        logger = LogManager.getFormatterLogger();
    }

    void start() throws InterruptedException, ExecutionException {
        while (true) {
            runProtocol();
        }
    }

    void runProtocol() throws InterruptedException, ExecutionException {
        List<Member> targetList = getRandomMembers(1, null);
        if (targetList.size() == 0) {
            logger.info("There are no members to PING");
            Thread.sleep(protocolPeriod);
            return;
        }

        Member target = targetList.get(0);

        try {
            logger.info("Sending PING to %s", target.toString());
            Message ack = dispatcher.ping(target, reqTimeout);
            logger.info("Received ACK from %s", target.toString());
        } catch (TimeoutException e) {
            logger.info("Timeout while waiting for ACK from %s", target.toString());
            List<Member> pingReqtargets = getRandomMembers(subgroupSize, target);
            if (pingReqtargets.size() == 0) {
                logger.info("There are no members to send a PING-REQ");
                removeMember(target);
                Thread.sleep(protocolPeriod);
                return;
            }
            try {
                logger.info("Sending PING-REQ to %d hosts", pingReqtargets.size());
                List<Message> messages = dispatcher.pingReq(pingReqtargets, target, reqTimeout);
                logger.info("Received ACK from Indirect Probes");
            } catch (TimeoutException e2) {
                logger.info("Timeout waiting for Indirect Probes", target.toString());
                removeMember(target);
            }
        }
        Thread.sleep(protocolPeriod);
    }

    List<Member> getRandomMembers(int k, Member targetToExclude) {
        Random rand = new Random();
        List<Member> randomMembers = new ArrayList<>();
        List<Member> selectionList = new ArrayList<>(membershipList);
        selectionList.remove(SwimJava.getSelf());
        selectionList.remove(targetToExclude);

        int selectionSize = Math.min(k, selectionList.size());
        for (int i = 0; i < selectionSize; i++) {
            int randomIndex = rand.nextInt(selectionList.size());
            randomMembers.add(selectionList.get(randomIndex));
            selectionList.remove(randomIndex);
        }

        return randomMembers;
    }

    void setReqTimeout(int timeoutInMillis) {
        reqTimeout = timeoutInMillis;
    }

    void setProtocolPeriod(int protocolPeriodInMillis) {
        protocolPeriod = protocolPeriodInMillis;
    }

    private void removeMember(Member target) {
        logger.info("Dropping %s from membership list", target.toString());
        membershipList.remove(null);
    }
}
