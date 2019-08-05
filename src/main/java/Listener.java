import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Listener {

    private final Logger logger = LogManager.getFormatterLogger();
    private Dispatcher dispatcher;
    private Config conf;

    Listener(Dispatcher dispatcher, Config conf) {
        this.dispatcher = dispatcher;
        this.conf = conf;
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
                    dispatcher.ping(message.getIndirectProbeMember(), conf.getReqTimeout());
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
}
