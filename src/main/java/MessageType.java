import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    PING(1),
    PING_REQ(2),
    ACK(3);

    private final int value;
    private static final Map<Integer, MessageType> lookup = new HashMap<>();

    static {
        for (MessageType type : MessageType.values()) {
            lookup.put(type.getValue(), type);
        }
    }

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType getType(int value) {
        return lookup.get(value);
    }

    public int getValue() {
        return value;
    }
}