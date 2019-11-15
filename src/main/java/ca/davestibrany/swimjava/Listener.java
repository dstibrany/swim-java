package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Listener {

    private final Logger logger = LogManager.getLogger();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
                logger.info("Received PING from {}", message.getMember());
                dispatcher.ack(message.getMember());
                logger.info("Sending ACK to {}", message.getMember());
                break;

            case PING_REQ:
                logger.info("Received PING-REQ from {} for {}",
                        message.getMember(),
                        message.getIndirectProbeMember());

                executor.submit(() -> {
                    try {
                        dispatcher.ping(message.getIndirectProbeMember(), conf.getReqTimeout());
                        dispatcher.ack(message.getMember());
                        logger.info("Sending ACK to {}", message.getMember());
                    } catch (TimeoutException e) {
                        logger.info("Timeout waiting for indirect probe to {}", message.getMember());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                break;

            case ACK:
                logger.info("Received ACK from {}...dropping", message.getMember());
                break;

            case JOIN:
                logger.info("Received JOIN from {}", message.getMember());
                dispatcher.joinAck(message.getMember());
                break;

            default:
                logger.trace("Dropping unknown message type");
                break;
        }
    }
}
