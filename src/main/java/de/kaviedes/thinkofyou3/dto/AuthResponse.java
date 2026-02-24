package de.kaviedes.thinkofyou3.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String refreshToken;

    public AuthResponse(String token, String username) {
        this(token, username, null);
    }

    public AuthResponse(String token, String username, String refreshToken) {
        this.token = token;
        this.username = username;
        this.refreshToken = refreshToken;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRefreshToken() { return refreshToken; }
}
