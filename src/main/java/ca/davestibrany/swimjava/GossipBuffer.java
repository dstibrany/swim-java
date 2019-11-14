package ca.davestibrany.swimjava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class GossipBuffer {
    private final ConcurrentHashMap<Member, Gossip> bufferElements; // TODO: remove old elements
    private final int expirationMultiplier;

    GossipBuffer(ConcurrentHashMap<Member, Gossip> bufferElements, int expirationMultiplier) {
        this.bufferElements = bufferElements;
        this.expirationMultiplier = expirationMultiplier;
    }

    List<Gossip> getItems(int n, int memberListSize) {
        List<Gossip> gossipItems = new ArrayList<>(bufferElements.values())
                .stream()
                .filter(g -> !g.isExpired())
                .sorted()
                .limit(n)
                .collect(Collectors.toList());

        gossipItems.forEach(g -> expireGossip(g, memberListSize));

        return gossipItems;
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
        // gossip buffer slot is empty
        if (g2 == null) return true;

        // we already have this item
        if (g1.equals(g2)) return false;

        if (g2.isExpired()) return true;

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

    private void expireGossip(Gossip g, int memberListSize) {
        if (g.getPiggyBackCount() >= expirationMultiplier * Math.max(log2(memberListSize), 1)) {
            g.setExpired();
        }
    }

    private int log2(int n) {
        return (int) Math.round(Math.log(n) / Math.log(2));
    }
}
