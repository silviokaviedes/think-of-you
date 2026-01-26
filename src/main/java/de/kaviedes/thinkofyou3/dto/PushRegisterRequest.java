package de.kaviedes.thinkofyou3.dto;

public class PushRegisterRequest {
    private String token;
    private String platform;

    /**
     * Default constructor for JSON deserialization.
     */
    public PushRegisterRequest() {
    }

    /**
     * Creates a push registration request.
     *
     * @param token    raw FCM device token
     * @param platform platform identifier (e.g. android, ios)
     */
    public PushRegisterRequest(String token, String platform) {
        this.token = token;
        this.platform = platform;
    }

    /**
     * @return raw FCM device token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return platform identifier (e.g. android, ios)
     */
    public String getPlatform() {
        return platform;
    }
}
