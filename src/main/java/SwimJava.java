import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwimJava {
    private static Member self;

    static Member getSelf() {
        return self;
    }

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        Config conf = new Config();
        self = new Member(conf.getPort(), conf.getAddress());
        List<Member> memberList = new ArrayList<>();
        memberList.add(self);
        Disseminator disseminator = new Disseminator(memberList);
        Dispatcher dispatcher = new Dispatcher(new TransportFactory(), disseminator, conf);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            List<Member> seeds = conf.getSeeds();
            if (!seeds.contains(self)) {
                Member seed = seeds.get(0); // TODO: round robin
                try {
                    logger.info("Joining cluster via seed node {}", seed.toString());
                    dispatcher.join(seed, conf.getReqTimeout());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            FailureDetector fd = new FailureDetector(memberList, dispatcher, conf);
            try {
                fd.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

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
}
