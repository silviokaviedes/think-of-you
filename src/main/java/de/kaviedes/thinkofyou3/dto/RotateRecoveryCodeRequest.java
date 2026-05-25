package de.kaviedes.thinkofyou3.dto;

public class RotateRecoveryCodeRequest {
    private String currentPassword;
    private String recoveryEmail;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getRecoveryEmail() {
        return recoveryEmail;
    }

    public void setRecoveryEmail(String recoveryEmail) {
        this.recoveryEmail = recoveryEmail;
    }
}
