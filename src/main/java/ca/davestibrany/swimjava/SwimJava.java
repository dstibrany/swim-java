package ca.davestibrany.swimjava;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwimJava {
    private static Member self;
    private static ExecutorService executorService;
    private static Config conf;
    private static MemberList memberList;
    private static Dispatcher dispatcher;
    private static Disseminator disseminator;

    // TODO: expose public API
    public static void main(String[] args) {
        init();
        joinCluster();
        startListener();
        startFailureDetector();
    }

    private static void init() {
        executorService = Executors.newFixedThreadPool(2);
        conf = new Config();
        self = conf.getSelf();
        memberList = new MemberList(self);
        disseminator = new Disseminator(
                memberList,
                new GossipBuffer(new ConcurrentHashMap<>(), conf.getExpirationMultiplier()),
                conf);
        dispatcher = new Dispatcher(new NetTransportFactory(), disseminator, conf);
    }

    private static void joinCluster() {
        Join join = new Join(conf.getSeeds(), self, dispatcher, conf.getJoinTimeout());
        try {
            join.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
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
