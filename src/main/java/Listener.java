import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Listener {
    private final Logger logger;
    private Dispatcher dispatcher;

    Listener(Dispatcher dispatcher) {
        logger = LogManager.getFormatterLogger();
        this.dispatcher = dispatcher;
    }

    public void start() {
        while (true) {
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
                    dispatcher.indirectProbe(message.getMember(), message.getIndirectProbeMember());
                    logger.info("Sent ACK to %s", message.getMember().toString());
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
}
