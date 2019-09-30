package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Simulation {
    private final int maxRounds;
    private volatile int round = 1;
    private List<SimulationNode> nodes;
    private final Lock roundLock = new ReentrantLock();
    private final Condition joinCondition = roundLock.newCondition();
    private final PriorityQueue<SimulationNode> joinQueue;

    Simulation(List<SimulationNode> nodes, int maxRounds) {
        this.nodes = new CopyOnWriteArrayList<>();
        this.maxRounds = maxRounds;
        joinQueue = new PriorityQueue<>();
        joinQueue.addAll(nodes);
    }

    List<SimulationNode> getState() {
        return nodes;
    }

    void run() throws ExecutionException, InterruptedException {
        Logger logger = LogManager.getLogger();
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Future<?> fdFuture = executorService.submit(() -> {
            while (!executorService.isShutdown()) {
                try {
                    logger.info("Current round: {}", round);

                    if (round == 5) {
//                        nodes.remove(node2);
                    }

                    if (round == maxRounds) {
                        executorService.shutdown();
                        break;
                    }

                    for (SimulationNode node : nodes) {
                        logger.info("Running FailureDetector for {}", node.getSelf());
                        node.getFailureDetector().runProtocol();
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
                    for (SimulationNode node : nodes) {
                        node.getListener().listenerProtocol();
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
                    SimulationNode node = joinQueue.poll();
                    roundLock.lock();
                    try {
                        while (round != node.getJoinTime()) {
                            joinCondition.await();
                        }
                        logger.info("Running Join for {}", node.getSelf());
                        node.getJoin().start();
                        nodes.add(node);
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


}
