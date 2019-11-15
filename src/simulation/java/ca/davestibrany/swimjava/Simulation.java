package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

class Simulation {
    private final int maxRounds;
    private final PriorityQueue<SimulationNode> joinQueue;
    private int round = 1;
    private List<SimulationNode> nodes;

    Simulation(List<SimulationNode> nodes, int maxRounds) {
        this.nodes = new CopyOnWriteArrayList<>();
        this.maxRounds = maxRounds;
        joinQueue = new PriorityQueue<>();
        joinQueue.addAll(nodes);
    }

    void run() throws ExecutionException, InterruptedException {
        Logger logger = LogManager.getLogger();
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> fdFuture = executorService.submit(() -> {
            while (!executorService.isShutdown()) {
                try {
                    logger.info("Current round: {}", round);

                    if (round == maxRounds + 1) {
                        logger.info("Exiting simulation");
                        executorService.shutdown();
                        break;
                    }

                    if (!joinQueue.isEmpty() && round == joinQueue.peek().getJoinTime()) {
                        SimulationNode node = joinQueue.poll();
                        logger.info("Running Join for {}", node.getSelf());
                        node.getJoin().start();
                        nodes.add(node);
                    }

                    for (SimulationNode node : nodes) {
                        if (round == node.getFailureTime()) {
                            nodes.remove(node);
                            node.alive = false;
                            continue;
                        }

                        logger.info("Running FailureDetector for {}", node.getSelf());
                        node.getFailureDetector().runProtocol();
                    }

                    round++;
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
                    Thread.sleep(5);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });

        fdFuture.get();
    }
}
