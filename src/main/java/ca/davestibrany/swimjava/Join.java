package ca.davestibrany.swimjava;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class Join {
    private final List<Member> seeds;
    private final Member self;
    private final Logger logger = LogManager.getLogger();
    private final Dispatcher dispatcher;
    private final int joinTimeout;

    Join(List<Member> seeds, Member self, Dispatcher dispatcher, int joinTimeout) {
        this.seeds = seeds;
        this.self = self;
        this.dispatcher = dispatcher;
        this.joinTimeout = joinTimeout;
    }

    void start() throws InterruptedException, ExecutionException {
        if (seeds.contains(self)) {
            joinAsSeed();
        } else {
            joinAsNonSeed();
        }
    }

    private void joinAsSeed() throws InterruptedException, ExecutionException {
        int connectAttempts = 0;
        List<Member> seedsWithoutSelf = new ArrayList<>(seeds);
        seedsWithoutSelf.remove(self);

        for (Member seed : seedsWithoutSelf) {
            connectAttempts++;
            try {
                logger.info("Joining cluster via seed node {}", seed);
                dispatcher.join(seed, joinTimeout);
            } catch (TimeoutException e) {
                if (connectAttempts < seedsWithoutSelf.size()) {
                    logger.warn("Failed to connect via seed node {}. Trying next seed node...", seed);
                } else {
                    logger.info("Failed to join an existing cluster, starting up new cluster as a seed node");
                    break;
                }
                continue;
            }
            break;
        }
    }

    private void joinAsNonSeed() throws InterruptedException, ExecutionException {
        int connectAttempts = 0;

        for (Member seed : seeds) {
            connectAttempts++;
            try {
                logger.info("Joining cluster via seed node {}", seed);
                dispatcher.join(seed, joinTimeout);
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
    }
}
