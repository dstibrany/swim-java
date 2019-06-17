import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Listener {
    private Transport transport;
    private Logger logger;

    Listener(Transport transport) {
        this.transport = transport;
        logger = LogManager.getFormatterLogger();
    }

    public void start() throws IOException {
        while (true) {
            Message message = transport.receive();
            switch (message.getMessageType()) {
                case PING:
                    logger.info("Received PING from %s:%d", message.getSrcAddress().getHostAddress(),  message.getSrcPort());
                    Message ack = new Message(MessageType.ACK, message.getSrcPort(), message.getSrcAddress());
                    transport.send(ack);
                    logger.info("Sent ACK to %s:%d",message.getSrcAddress().getHostAddress(), message.getSrcPort());
                    break;
                case PING_REQ:
                    logger.info("Received PING-REQ from %s:%d", message.getSrcAddress().getHostAddress(),  message.getSrcPort());
//                    Message indirectPing = new Message(MessageType.PING);
//                    transport.send(indirectPing);
                    Message indirectAck = transport.receive();
                    transport.send(indirectAck);
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
