import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class GossipBuffer {
    private final Map<Member, Gossip> bufferElements;

    GossipBuffer(Map<Member, Gossip> bufferElements) {
        this.bufferElements = bufferElements;
    }

    // TODO: get this working
    List<Gossip> getItems(int n) {
        return new ArrayList<>(bufferElements.values());
    }

    boolean mergeItem(Gossip gossip) {
        boolean wasMerged = false;
        if (overrides(gossip, bufferElements.get(gossip.getMember()))) {
            bufferElements.put(gossip.getMember(), gossip);
            wasMerged = true;
        }
        return wasMerged;
    }

    boolean overrides(Gossip g1, Gossip g2) {
        if (g2 == null) return true;

        if (!g1.getMember().equals(g2.getMember())) {
            throw new RuntimeException("GossipBuffer.overrides must be called on the same Member");
        }

        switch (g1.getGossipType()) {
            case ALIVE:
                if (g2.getGossipType() == GossipType.SUSPECT && g1.getIncarnationNumber() > g2.getIncarnationNumber()) {
                    return true;
                } else if (g2.getGossipType() == GossipType.ALIVE && g1.getIncarnationNumber() > g2.getIncarnationNumber()) {
                    return true;
                }
                break;
            case SUSPECT:
                if (g2.getGossipType() == GossipType.SUSPECT && g1.getIncarnationNumber() > g2.getIncarnationNumber()) {
                    return true;
                } else if (g2.getGossipType() == GossipType.ALIVE && g1.getIncarnationNumber() >= g2.getIncarnationNumber()) {
                    return true;
                }
                break;
            case CONFIRM:
                if (g2.getGossipType() == GossipType.SUSPECT || g2.getGossipType() == GossipType.ALIVE) {
                    return true;
                }
                break;
            case JOIN:
                return true;
        }

        return false;
    }
}
