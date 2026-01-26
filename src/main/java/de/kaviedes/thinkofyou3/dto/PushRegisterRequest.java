package de.kaviedes.thinkofyou3.dto;

public class PushRegisterRequest {
    private String token;
    private String platform;

    public PushRegisterRequest() {
    }

    public PushRegisterRequest(String token, String platform) {
        this.token = token;
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public String getPlatform() {
        return platform;
    }
}
