import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class FailureDetector {
    private final Logger logger = LogManager.getLogger();
    private final Disseminator disseminator;
    private final MemberList memberList;
    private final Dispatcher dispatcher;
    private final Config conf;

    FailureDetector(MemberList memberList, Dispatcher dispatcher, Disseminator disseminator, Config conf) {
        this.memberList = memberList;
        this.dispatcher = dispatcher;
        this.conf = conf;
        this.disseminator = disseminator;
    }

    void start() throws InterruptedException, ExecutionException {
        while (true) {
            runProtocol();
        }
    }

    void runProtocol() throws InterruptedException, ExecutionException {
        logger.debug("{}", memberList);
        List<Member> targetList = memberList.getRandomMembers(1, null);
        if (targetList.size() == 0) {
            logger.info("There are no members to PING");
            Thread.sleep(conf.getProtocolPeriod());
            return;
        }
        Member target = targetList.get(0);

        try {
            logger.info("Sending PING to {}", target);
            dispatcher.ping(target, conf.getReqTimeout());
            logger.info("Received ACK from {}", target);
        } catch (TimeoutException e) {
            logger.info("Timeout while waiting for ACK from {}", target);
            List<Member> pingReqtargets = memberList.getRandomMembers(conf.getSubgroupSize(), target);
            if (pingReqtargets.size() == 0) {
                logger.info("There are no members to send a PING-REQ");
                disseminator.suspect(target);
                Thread.sleep(conf.getProtocolPeriod());
                return;
            }
            try {
                logger.info("Sending PING-REQ to {} hosts", pingReqtargets.size());
                dispatcher.pingReq(pingReqtargets, target, conf.getReqTimeout());
                logger.info("Received ACK from Indirect Probes");
            } catch (TimeoutException e2) {
                logger.info("Timeout waiting for Indirect Probes");
                disseminator.suspect(target);
            }
        }
        Thread.sleep(conf.getProtocolPeriod());
    }

}
