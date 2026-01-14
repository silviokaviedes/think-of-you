package de.kaviedes.thinkofyou3.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "thought_events")
public class ThoughtEvent {
    @Id
    private String id;
    private String connectionId;
    private String senderId;
    private String recipientId;
    private Instant occurredAt;

    public ThoughtEvent() {}

    public ThoughtEvent(String connectionId, String senderId, String recipientId) {
        this.connectionId = connectionId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.occurredAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
