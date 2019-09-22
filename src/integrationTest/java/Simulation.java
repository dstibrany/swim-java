import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Simulation {
    private final PriorityQueue<SwimJavaWrapper> joinQueue;
    private int round;
    private List<SwimJavaWrapper> nodes;
    private final Lock roundLock = new ReentrantLock();
    private final Condition joinCondition  = roundLock.newCondition();
    private final Map<Member, BlockingQueue<Message>> failureDetectorQueues = new ConcurrentHashMap<>();
    private final Map<Member, Queue<Message>> listenerQueues = new ConcurrentHashMap<>();

    Simulation() {
        nodes = new CopyOnWriteArrayList<>();
        joinQueue = new PriorityQueue<>();
        round = 1;
    }

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Simulation s = new Simulation();
        SwimJavaWrapper node1 = s.build(5555, 1);
        SwimJavaWrapper node2 = s.build(5556, 2);
        SwimJavaWrapper node3 = s.build(5557, 3);

        executorService.submit(() -> {
            while (true) {
                try {
                    logger.info("Current round: {}", s.round);

                    if (s.round == 5) {
                        s.nodes.remove(node2);
                    }

                    for (SwimJavaWrapper swimJavaWrapper : s.nodes) {
                        logger.info("Running FailureDetector for {}", swimJavaWrapper.self);
                        swimJavaWrapper.getFailureDetector().runProtocol();
                    }

                    s.roundLock.lock();
                    try {
                        s.round++;
                        s.joinCondition.signal();
                    } finally {
                        s.roundLock.unlock();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

        executorService.submit(() -> {
            while (true) {
                try {
                    for (SwimJavaWrapper swimJavaWrapper : s.nodes) {
                        swimJavaWrapper.getListener().listenerProtocol();
                    }
                    Thread.sleep(500);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

        executorService.submit(() -> {
            try {
                while (!s.joinQueue.isEmpty()) {
                    SwimJavaWrapper swimJavaWrapper = s.joinQueue.poll();
                    s.roundLock.lock();
                    try {
                        while (s.round != swimJavaWrapper.getJoinAtRound()) {
                            s.joinCondition.await();
                        }
                        logger.info("Running Join for {}", swimJavaWrapper.self);
                        swimJavaWrapper.getJoin().start();
                        s.nodes.add(swimJavaWrapper);
                    } finally {
                        s.roundLock.unlock();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private SwimJavaWrapper build(int port, int initRound) {
        com.typesafe.config.Config mergedConf = ConfigFactory
                .parseString("swim-java.port=" + port)
                .withFallback(ConfigFactory.defaultReference());
        Config conf = new Config(mergedConf);
        Member self = conf.getSelf();
        MemberList memberList = new MemberList(self);
        Disseminator disseminator = new Disseminator(memberList, new GossipBuffer(new ConcurrentHashMap<>()), conf);
        Dispatcher dispatcher = new Dispatcher(new TestTransportFactory(self, listenerQueues, failureDetectorQueues), disseminator, conf);
        Join join = new Join(conf.getSeeds(), self, dispatcher, conf.getJoinTimeout());
        FailureDetector fd = new FailureDetector(memberList, dispatcher, disseminator, conf);
        Listener listener = new Listener(dispatcher, conf);
        SwimJavaWrapper swimJavaWrapper = new SwimJavaWrapper(initRound, self, memberList, disseminator, dispatcher, fd, listener, join);
        joinQueue.add(swimJavaWrapper);
        return swimJavaWrapper;
    }

    static class SwimJavaWrapper implements Comparable<SwimJavaWrapper> {
        private final Member self;
        private final MemberList memberList;
        private final Disseminator disseminator;
        private final Dispatcher dispatcher;
        private final FailureDetector failureDetector;
        private final int joinAtRound;


        private final Listener listener;
        private final Join join;

        SwimJavaWrapper(
                int joinAtRound,
                Member self,
                MemberList memberList,
                Disseminator disseminator,
                Dispatcher dispatcher,
                FailureDetector failureDetector,
                Listener listener,
                Join join
        ) {
            this.joinAtRound = joinAtRound;
            this.self = self;
            this.memberList = memberList;
            this.disseminator = disseminator;
            this.dispatcher = dispatcher;
            this.failureDetector = failureDetector;
            this.listener = listener;
            this.join = join;

        }

        @Override
        public int compareTo(SwimJavaWrapper o) {
            return this.joinAtRound - o.getJoinAtRound();
        }

        int getJoinAtRound() {
            return joinAtRound;
        }

        public Member getSelf() {
            return self;
        }

        public MemberList getMemberList() {
            return memberList;
        }

        public Disseminator getDisseminator() {
            return disseminator;
        }

        public Dispatcher getDispatcher() {
            return dispatcher;
        }

        public FailureDetector getFailureDetector() {
            return failureDetector;
        }

        public Listener getListener() {
            return listener;
        }

        public Join getJoin() {
            return join;
        }
    }
}
