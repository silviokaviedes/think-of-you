package de.kaviedes.thinkofyou3.dto;

import de.kaviedes.thinkofyou3.model.Connection;
import de.kaviedes.thinkofyou3.model.Mood;

public class ConnectionDTO {
    private String id;
    private String partnerUsername;
    private int receivedClicks;
    private int sentClicks;
    private Connection.Status status;
    private Mood lastReceivedMood;

    public ConnectionDTO() {}

    public ConnectionDTO(String id, String partnerUsername, int receivedClicks, int sentClicks, Connection.Status status) {
        this(id, partnerUsername, receivedClicks, sentClicks, status, null);
    }

    public ConnectionDTO(String id, String partnerUsername, int receivedClicks, int sentClicks, Connection.Status status, Mood lastReceivedMood) {
        this.id = id;
        this.partnerUsername = partnerUsername;
        this.receivedClicks = receivedClicks;
        this.sentClicks = sentClicks;
        this.status = status;
        this.lastReceivedMood = lastReceivedMood;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPartnerUsername() { return partnerUsername; }
    public void setPartnerUsername(String partnerUsername) { this.partnerUsername = partnerUsername; }
    public int getReceivedClicks() { return receivedClicks; }
    public void setReceivedClicks(int receivedClicks) { this.receivedClicks = receivedClicks; }
    public int getSentClicks() { return sentClicks; }
    public void setSentClicks(int sentClicks) { this.sentClicks = sentClicks; }
    public Connection.Status getStatus() { return status; }
    public void setStatus(Connection.Status status) { this.status = status; }
    public Mood getLastReceivedMood() { return lastReceivedMood; }
    public void setLastReceivedMood(Mood lastReceivedMood) { this.lastReceivedMood = lastReceivedMood; }
}
