// messaging/Message.java
package messaging;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        KEY_EXCHANGE,
        TEXT_MESSAGE
    }

    private final UUID id;
    private final Type type;
    private final byte[] content;
    private final long timestamp;

    public Message(Type type, byte[] content) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", type=" + type +
                ", timestamp=" + timestamp +
                '}';
    }
}