import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class FailureDetector {
    private final Logger logger = LogManager.getLogger();
    private List<Member> membershipList;
    private Dispatcher dispatcher;
    private Config conf;

    FailureDetector(List<Member> membershipList, Dispatcher dispatcher, Config conf) {
        this.membershipList = membershipList;
        this.dispatcher = dispatcher;
        this.conf = conf;
    }

    void start() throws InterruptedException, ExecutionException {
        while (true) {
            runProtocol();
        }
    }

    //
    void runProtocol() throws InterruptedException, ExecutionException {
        List<Member> targetList = getRandomMembers(1, null);
        if (targetList.size() == 0) {
            logger.info("There are no members to PING");
            Thread.sleep(conf.getProtocolPeriod());
            return;
        }
        Member target = targetList.get(0);

        try {
            logger.info("Sending PING to {}", target.toString());
            dispatcher.ping(target, conf.getReqTimeout());
            logger.info("Received ACK from {}", target.toString());
        } catch (TimeoutException e) {
            logger.info("Timeout while waiting for ACK from {}", target.toString());
            List<Member> pingReqtargets = getRandomMembers(conf.getSubgroupSize(), target);
            if (pingReqtargets.size() == 0) {
                logger.info("There are no members to send a PING-REQ");
                suspectMember(target);
                Thread.sleep(conf.getProtocolPeriod());
                return;
            }
            try {
                logger.info("Sending PING-REQ to {} hosts", pingReqtargets.size());
                dispatcher.pingReq(pingReqtargets, target, conf.getReqTimeout());
                logger.info("Received ACK from Indirect Probes");
            } catch (TimeoutException e2) {
                logger.info("Timeout waiting for Indirect Probes");
                suspectMember(target);
            }
        }
        Thread.sleep(conf.getProtocolPeriod());
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

    private void suspectMember(Member target) {
        logger.info("Marking {} as suspected", target.toString());
        target.suspect();
    }
}
