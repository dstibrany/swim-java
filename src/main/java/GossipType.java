import java.util.HashMap;
import java.util.Map;

enum GossipType {
    ALIVE(1),
    SUSPECT(2),
    CONFIRM(3),
    JOIN(4),
    UNKNOWN(99);

    private static final Map<Integer, GossipType> lookup = new HashMap<>();

    static {
        for (GossipType type : GossipType.values()) {
            lookup.put(type.getValue(), type);
        }
    }

    private final int value;

    static final int BYTES = Integer.BYTES;

    GossipType(int value) {
        this.value = value;
    }

    static GossipType getType(int value) {
        GossipType type = lookup.get(value);
        if (type == null) {
            return GossipType.UNKNOWN;
        } else {
            return type;
        }
    }

    int getValue() {
        return value;
    }
}
