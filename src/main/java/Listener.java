import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Listener {
    private final Logger logger;
    private Dispatcher dispatcher;
    private int reqTimeout = 2000;

    Listener(Dispatcher dispatcher) {
        logger = LogManager.getFormatterLogger();
        this.dispatcher = dispatcher;
    }

    void start() throws InterruptedException, ExecutionException {
        while (true) {
            listenerProtocol();
        }
    }

    void listenerProtocol() throws InterruptedException, ExecutionException {
        Message message = dispatcher.receive();
        switch (message.getMessageType()) {
            case PING:
                logger.info("Received PING from %s", message.getMember().toString());
                dispatcher.ack(message.getMember());
                logger.info("Sent ACK to %s", message.getMember().toString());
                break;
            case PING_REQ:
                logger.info("Received PING-REQ from %s for %s",
                        message.getMember().toString(),
                        message.getIndirectProbeMember().toString());
                try {
                    Message ack = dispatcher.ping(message.getIndirectProbeMember(), reqTimeout);
                    dispatcher.ack(message.getMember());
                    logger.info("Sent ACK to %s", message.getMember().toString());
                } catch (TimeoutException e) {
                    logger.info("Timeout waiting for indirect probe to %s", message.getMember().toString());
                }
                break;
            case ACK:
                logger.info("Received ACK from %s...dropping", message.getMember().toString());
                break;
            default:
                logger.info("Dropping unknown message type");
                break;
        }
    }

    void setReqTimeout(int timeoutInMillis) {
        reqTimeout = timeoutInMillis;
    }

}
