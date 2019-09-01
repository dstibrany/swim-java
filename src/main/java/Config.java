import com.typesafe.config.ConfigFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class Config {
    private final int maxGossipPerMessage;
    private final int port;
    private  InetAddress address;
    private final List<Member> seeds = new ArrayList<>();
    private final int reqTimeout;
    private final int protocolPeriod;
    private final int subgroupSize;
    private final Member self;
    private final int suspicionTimeout;

    Config() {
        this(ConfigFactory.load());
    }

    Config(com.typesafe.config.Config conf) {
        conf.checkValid(ConfigFactory.defaultReference(), "swim-java");

        try {
            address = InetAddress.getByName(conf.getString("swim-java.address"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String[] parsedSeeds = conf.getString("swim-java.seeds").split(",");
        try {
            for (String seed : parsedSeeds) {
                String address = seed.substring(0, seed.indexOf(":"));
                String port = seed.substring(seed.indexOf(":") + 1);
                seeds.add(new Member(Integer.parseInt(port), InetAddress.getByName(address)));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        port = conf.getInt("swim-java.port");
        reqTimeout = conf.getInt("swim-java.request_timeout");
        protocolPeriod = conf.getInt("swim-java.protocol_period");
        subgroupSize = conf.getInt("swim-java.subgroup_size");
        maxGossipPerMessage = conf.getInt("swim-java.max_gossip_per_message");
        self = new Member(port, address);
        suspicionTimeout = conf.getInt("swim-java.suspicion_timeout");
    }

    int getPort() {
        return port;
    }

    InetAddress getAddress() {
        return address;
    }

    List<Member> getSeeds() {
        return seeds;
    }

    int getReqTimeout() {
        return reqTimeout;
    }

    int getProtocolPeriod() {
        return protocolPeriod;
    }

    int getSubgroupSize() {
        return subgroupSize;
    }

    int getMaxGossipPerMessage() {
        return maxGossipPerMessage;
    }

    Member getSelf() {
        return self;
    }

    int getSuspicionTimeout() {
        return suspicionTimeout;
    }


}
