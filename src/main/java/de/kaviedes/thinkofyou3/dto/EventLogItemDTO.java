package de.kaviedes.thinkofyou3.dto;

import java.time.Instant;

public class EventLogItemDTO {
    private String connectionId;
    private String partnerUsername;
    private String direction;
    private String mood;
    private Instant occurredAt;

    public EventLogItemDTO() {
    }

    public EventLogItemDTO(String connectionId, String partnerUsername, String direction, String mood, Instant occurredAt) {
        this.connectionId = connectionId;
        this.partnerUsername = partnerUsername;
        this.direction = direction;
        this.mood = mood;
        this.occurredAt = occurredAt;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getPartnerUsername() {
        return partnerUsername;
    }

    public void setPartnerUsername(String partnerUsername) {
        this.partnerUsername = partnerUsername;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
