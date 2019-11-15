package ca.davestibrany.swimjava;

import com.typesafe.config.ConfigFactory;

import java.util.concurrent.ConcurrentHashMap;

class SimulationNode implements Comparable<SimulationNode> {
    volatile boolean alive = true;
    private int joinTime;
    private int failureTime;
    private Config conf;
    private FailureDetector fd;
    private Listener listener;
    private Join join;
    private Dispatcher dispatcher;
    private Disseminator disseminator;
    private MemberList memberList;
    private Member self;

    int getJoinTime() {
        return joinTime;
    }

    int getFailureTime() {
        return failureTime;
    }

    Member getSelf() {
        return self;
    }

    MemberList getMemberList() {
        return memberList;
    }

    FailureDetector getFailureDetector() {
        return fd;
    }

    Listener getListener() {
        return listener;
    }

    Join getJoin() {
        return join;
    }

    Config getConf() {
        return conf;
    }

    @Override
    public int compareTo(SimulationNode o) {
        return this.joinTime - o.getJoinTime();
    }

    static class Builder {
        private int joinTime = 1;
        private int failureTime;
        private double dropProbability;
        private int suspicionTimeout;
        private int port;

        Builder withJoinTime(int joinTime) {
            this.joinTime = joinTime;
            return this;
        }

        Builder withFailureTime(int failureTime) {
            this.failureTime = failureTime;
            return this;
        }

        Builder withDropProbability(double dropProbability) {
            this.dropProbability = dropProbability;
            return this;
        }

        Builder withSuspicionTimeout(int suspicionTimeout) {
            this.suspicionTimeout = suspicionTimeout;
            return this;
        }

        SimulationNode build(int port, SimulationQueues queues) {
            this.port = port;
            SimulationNode node = new SimulationNode();
            node.failureTime = this.failureTime;
            node.joinTime = this.joinTime;
            node.conf = getConfig();
            node.self = node.conf.getSelf();
            node.memberList = new MemberList(node.self);
            node.disseminator = new Disseminator(
                    node.memberList,
                    new GossipBuffer(new ConcurrentHashMap<>(), node.conf.getExpirationMultiplier()),
                    node.conf);
            node.dispatcher = new Dispatcher(
                    new TestTransportFactory(node.self, queues.getListenerQueues(), queues.getFailureDetectorQueues(), dropProbability),
                    node.disseminator,
                    node.conf);
            node.join = new Join(node.conf.getSeeds(), node.self, node.dispatcher, node.conf.getJoinTimeout());
            node.fd = new FailureDetector(node.memberList, node.dispatcher, node.disseminator, node.conf);
            node.listener = new Listener(node.dispatcher, node.conf);

            return node;
        }

        private Config getConfig() {
            com.typesafe.config.Config mergedConf;

            if (suspicionTimeout == 0) {
                mergedConf = ConfigFactory
                        .parseString("swim-java.port=" + port)
                        .withFallback(ConfigFactory.defaultReference());
            } else {
                mergedConf = ConfigFactory
                        .parseString("swim-java.port=" + port + ", swim-java.suspicion_timeout=" + suspicionTimeout)
                        .withFallback(ConfigFactory.defaultReference());
            }
            return new Config(mergedConf);
        }

    }
}
