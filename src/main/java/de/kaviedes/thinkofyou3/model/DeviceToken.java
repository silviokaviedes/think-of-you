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

    /**
     * Default constructor for Spring Data deserialization.
     */
    public DeviceToken() {
    }

    /**
     * Creates a new device token record for a user.
     *
     * @param userId   user id that owns the device token
     * @param token    raw FCM device token
     * @param platform platform identifier (e.g. android, ios)
     */
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

    /**
     * Updates the platform string (used when the same token is re-registered).
     *
     * @param platform platform identifier (e.g. android, ios)
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Updates the timestamps to reflect a fresh token registration.
     */
    public void touch() {
        Instant now = Instant.now();
        this.updatedAt = now;
        this.lastSeenAt = now;
    }
}
