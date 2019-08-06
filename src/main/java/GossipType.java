import java.util.HashMap;
import java.util.Map;

public enum GossipType {
    ALIVE(1),
    SUSPECT(2),
    CONFIRM(3),
    UNKNOWN(99);

    private static final Map<Integer, GossipType> lookup = new HashMap<>();

    static {
        for (GossipType type : GossipType.values()) {
            lookup.put(type.getValue(), type);
        }
    }

    private final int value;

    GossipType(int value) {
        this.value = value;
    }

    public static GossipType getType(int value) {
        GossipType type = lookup.get(value);
        if (type == null) {
            return GossipType.UNKNOWN;
        } else {
            return type;
        }
    }

    public int getValue() {
        return value;
    }
}
