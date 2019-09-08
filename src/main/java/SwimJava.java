import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;

public class SwimJava {
    private static Member self;
    private static Logger logger;
    private static ExecutorService executorService;
    private static Config conf;
    private static MemberList memberList;
    private static Dispatcher dispatcher;
    private static Disseminator disseminator;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        init();
        join();
        startListener();
        startFailureDetector();
    }

    private static void init() {
        executorService = Executors.newFixedThreadPool(2);
        logger = LogManager.getLogger();
        conf = new Config();
        self = conf.getSelf();
        memberList = new MemberList(self);
        disseminator = new Disseminator(memberList, new GossipBuffer(new ConcurrentHashMap<>()), conf);
        dispatcher = new Dispatcher(new TransportFactory(), disseminator, conf);
    }

    // TODO: handle joins as second seed node
    private static void join() throws InterruptedException, ExecutionException {
        List<Member> seeds = conf.getSeeds();

        if (!seeds.contains(self)) {
            int connectAttempts = 0;

            for (Member seed : seeds) {
                connectAttempts++;
                try {
                    logger.info("Joining cluster via seed node {}", seed);
                    dispatcher.join(seed, conf.getReqTimeout());
                } catch (TimeoutException e) {
                    if (connectAttempts < seeds.size()) {
                        logger.warn("Failed to connect via seed node {}. Trying next seed node...", seed);
                    } else {
                        logger.error("Could not connect to any seed nodes, exiting.");
                        System.exit(1);
                    }
                    continue;
                }
                break;
            }
        } else {
            logger.info("Starting up cluster as seed node");
        }
    }

    private static void startListener() {
        executorService.submit(() -> {
            Listener listener = new Listener(dispatcher, conf);
            try {
                listener.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void startFailureDetector() {
        executorService.submit(() -> {
            FailureDetector fd = new FailureDetector(memberList, dispatcher, disseminator, conf);
            try {
                fd.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
