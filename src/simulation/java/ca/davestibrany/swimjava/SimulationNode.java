package ca.davestibrany.swimjava;

import com.typesafe.config.ConfigFactory;

import java.util.concurrent.ConcurrentHashMap;

class SimulationNode implements Comparable<SimulationNode> {
    private int joinTime;
    private int failureTime;
    private int port;
    private Config conf;
    private FailureDetector fd;
    private Listener listener;
    private Join join;
    private Dispatcher dispatcher;
    private Disseminator disseminator;
    private MemberList memberList;
    private Member self;

    private SimulationNode() {

    }

     int getJoinTime() {
        return joinTime;
    }

    public int getFailureTime() {
        return failureTime;
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
        return fd;
    }

    public Listener getListener() {
        return listener;
    }

    public Join getJoin() {
        return join;
    }

    @Override
    public int compareTo(SimulationNode o) {
        return this.joinTime - o.getJoinTime();
    }

    static class Builder {
        private int joinTime;
        private int failureTime;
        private int port;
        private SimulationQueues queues;

        Builder withJoinTime(int joinTime) {
           this.joinTime = joinTime;
           return this;
        }

        Builder withFailureTime(int failureTime) {
            this.failureTime = failureTime;
            return this;
        }

        SimulationNode build(int port, SimulationQueues queues) {
            this.port = port;
            this.queues = queues;

            com.typesafe.config.Config mergedConf = ConfigFactory
                    .parseString("swim-java.port=" + port)
                    .withFallback(ConfigFactory.defaultReference());

            SimulationNode node = new SimulationNode();
            node.failureTime = this.failureTime;
            node.joinTime = this.joinTime;
            node.port = this.port;
            node.conf = new Config(mergedConf);
            node.self = node.conf.getSelf();
            node.memberList = new MemberList(node.self);
            node.disseminator = new Disseminator(node.memberList, new GossipBuffer(new ConcurrentHashMap<>()), node.conf);
            node.dispatcher = new Dispatcher(
                    new TestTransportFactory(node.self, queues.getListenerQueues(), queues.getFailureDetectorQueues()),
                    node.disseminator,
                    node.conf
            );
            node.join = new Join(node.conf.getSeeds(), node.self, node.dispatcher, node.conf.getJoinTimeout());
            node.fd = new FailureDetector(node.memberList, node.dispatcher, node.disseminator, node.conf);
            node.listener = new Listener(node.dispatcher, node.conf);

            return node;
        }

    }
}
