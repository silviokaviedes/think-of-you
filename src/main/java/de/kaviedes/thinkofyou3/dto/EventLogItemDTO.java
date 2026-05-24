package de.kaviedes.thinkofyou3.dto;

import java.time.Instant;

public class EventLogItemDTO {
    private String connectionId;
    private String partnerUsername;
    private String direction;
    private String mood;
    private EnergyLevelsDTO energy;
    private Instant occurredAt;

    public EventLogItemDTO() {
    }

    public EventLogItemDTO(String connectionId, String partnerUsername, String direction, String mood, Instant occurredAt) {
        this(connectionId, partnerUsername, direction, mood, null, occurredAt);
    }

    public EventLogItemDTO(String connectionId, String partnerUsername, String direction, String mood,
                           EnergyLevelsDTO energy, Instant occurredAt) {
        this.connectionId = connectionId;
        this.partnerUsername = partnerUsername;
        this.direction = direction;
        this.mood = mood;
        this.energy = energy;
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

    public EnergyLevelsDTO getEnergy() {
        return energy;
    }

    public void setEnergy(EnergyLevelsDTO energy) {
        this.energy = energy;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
