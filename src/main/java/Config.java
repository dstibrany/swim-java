import com.typesafe.config.ConfigFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class Config {
    private int port;
    private InetAddress address;
    private List<Member> seeds = new ArrayList<>();
    private int reqTimeout;
    private int protocolPeriod;
    private int subgroupSize;

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
}
