package de.kaviedes.thinkofyou3.dto;

public class LoginRequest {
    private String username;
    private String password;
    private String recoveryEmail;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRecoveryEmail() { return recoveryEmail; }
    public void setRecoveryEmail(String recoveryEmail) { this.recoveryEmail = recoveryEmail; }
}
