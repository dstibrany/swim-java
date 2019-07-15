import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Listener {
    private Transport transport;
    private Logger logger;
    private Messager messager = new Messager();

    Listener(Transport transport) {
        this.transport = transport;
        logger = LogManager.getFormatterLogger();
    }

    public void start() {
        while (true) {
            Message message = transport.receive();
            switch (message.getMessageType()) {
                case PING:
                    messager.ack(message.getMember());
                    break;
                case PING_REQ:
                    messager.indirectProbe(message.getMember(), message.getiProbeMember());
                    break;
                case ACK:
                    logger.info("Received ACK");
                    break;
                default:
                    logger.info("Dropping unknown message type");
                    break;
            }
        }
    }
}
