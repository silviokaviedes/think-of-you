package de.kaviedes.thinkofyou3.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "connections")
public class Connection {
    @Id
    private String id;
    private String requesterId;
    private String recipientId;
    private Status status;
    private int requesterToRecipientCount;
    private int recipientToRequesterCount;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED, DISCONNECTED
    }

    public Connection() {}

    public Connection(String requesterId, String recipientId) {
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.status = Status.PENDING;
        this.requesterToRecipientCount = 0;
        this.recipientToRequesterCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getRequesterToRecipientCount() { return requesterToRecipientCount; }
    public void setRequesterToRecipientCount(int requesterToRecipientCount) { this.requesterToRecipientCount = requesterToRecipientCount; }
    public int getRecipientToRequesterCount() { return recipientToRequesterCount; }
    public void setRecipientToRequesterCount(int recipientToRequesterCount) { this.recipientToRequesterCount = recipientToRequesterCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
