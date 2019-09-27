package ca.davestibrany.swimjava;

import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Simulation {
    private volatile int round;
    private List<SwimJavaState> nodes;
    private final Lock roundLock = new ReentrantLock();
    private final Condition joinCondition = roundLock.newCondition();
    private final PriorityQueue<SwimJavaState> joinQueue;
    private final Map<Member, BlockingQueue<Message>> failureDetectorQueues = new ConcurrentHashMap<>();
    private final Map<Member, Queue<Message>> listenerQueues = new ConcurrentHashMap<>();

    Simulation() {
        nodes = new CopyOnWriteArrayList<>();
        joinQueue = new PriorityQueue<>();
        round = 1;
    }

    List<SwimJavaState> getState() {
        return nodes;
    }

    void run() throws ExecutionException, InterruptedException {
        Logger logger = LogManager.getLogger();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        SwimJavaState node1 = build(5555, 1);
        SwimJavaState node2 = build(5556, 2);
        SwimJavaState node3 = build(5557, 3);

        Future<?> fdFuture = executorService.submit(() -> {
            while (!executorService.isShutdown()) {
                try {
                    logger.info("Current round: {}", round);

                    if (round == 5) {
                        nodes.remove(node2);
                    }

                    if (round == 10) {
                        executorService.shutdown();
                        break;
                    }

                    for (SwimJavaState swimJavaWrapper : nodes) {
                        logger.info("Running FailureDetector for {}", swimJavaWrapper.self);
                        swimJavaWrapper.getFailureDetector().runProtocol();
                    }

                    roundLock.lock();
                    try {
                        round++;
                        joinCondition.signal();
                    } finally {
                        roundLock.unlock();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

        executorService.submit(() -> {
            while (!executorService.isShutdown()) {
                try {
                    for (SwimJavaState swimJavaState : nodes) {
                        swimJavaState.getListener().listenerProtocol();
                    }
                    Thread.sleep(100);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

        executorService.submit(() -> {
            try {
                while (!joinQueue.isEmpty() && !executorService.isShutdown()) {
                    SwimJavaState swimJavaState = joinQueue.poll();
                    roundLock.lock();
                    try {
                        while (round != swimJavaState.getJoinAtRound()) {
                            joinCondition.await();
                        }
                        logger.info("Running Join for {}", swimJavaState.self);
                        swimJavaState.getJoin().start();
                        nodes.add(swimJavaState);
                    } finally {
                        roundLock.unlock();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        fdFuture.get();
    }

    private SwimJavaState build(int port, int initRound) {
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
        SwimJavaState swimJavaState = new SwimJavaState(initRound, self, memberList, disseminator, dispatcher, fd, listener, join);
        joinQueue.add(swimJavaState);
        return swimJavaState;
    }

    static class SwimJavaState implements Comparable<SwimJavaState> {
        private final Member self;
        private final MemberList memberList;
        private final Disseminator disseminator;
        private final Dispatcher dispatcher;
        private final FailureDetector failureDetector;
        private final int joinAtRound;
        private final Listener listener;
        private final Join join;

        SwimJavaState(
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
        public int compareTo(SwimJavaState o) {
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

    static class SwimJavaConfig {

    }
}
