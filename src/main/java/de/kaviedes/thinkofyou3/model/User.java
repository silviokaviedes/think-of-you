package de.kaviedes.thinkofyou3.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String passwordHash;
    private List<String> favoriteMoods;
    private DashboardDisplayMode dashboardDisplayMode;
    private Instant createdAt;

    public User() {}

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.favoriteMoods = new ArrayList<>(Mood.defaultFavorites());
        this.dashboardDisplayMode = DashboardDisplayMode.COUNTS;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<String> getFavoriteMoods() { return favoriteMoods; }
    public void setFavoriteMoods(List<String> favoriteMoods) { this.favoriteMoods = favoriteMoods; }
    public DashboardDisplayMode getDashboardDisplayMode() { return dashboardDisplayMode; }
    public void setDashboardDisplayMode(DashboardDisplayMode dashboardDisplayMode) { this.dashboardDisplayMode = dashboardDisplayMode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
