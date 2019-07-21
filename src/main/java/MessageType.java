import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    PING(1),
    PING_REQ(2),
    ACK(3),
    UNKNOWN(4);

    private static final Map<Integer, MessageType> lookup = new HashMap<>();

    static {
        for (MessageType type : MessageType.values()) {
            lookup.put(type.getValue(), type);
        }
    }

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType getType(int value) {
        MessageType type = lookup.get(value);
        if (type == null) {
            return MessageType.UNKNOWN;
        } else {
            return type;
        }
    }

    public int getValue() {
        return value;
    }
}