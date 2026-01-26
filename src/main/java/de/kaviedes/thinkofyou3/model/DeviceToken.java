package de.kaviedes.thinkofyou3.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "deviceTokens")
public class DeviceToken {
    @Id
    private String id;
    private String userId;
    private String token;
    private String platform;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSeenAt;

    public DeviceToken() {
    }

    public DeviceToken(String userId, String token, String platform) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastSeenAt = now;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getPlatform() {
        return platform;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void touch() {
        Instant now = Instant.now();
        this.updatedAt = now;
        this.lastSeenAt = now;
    }
}
