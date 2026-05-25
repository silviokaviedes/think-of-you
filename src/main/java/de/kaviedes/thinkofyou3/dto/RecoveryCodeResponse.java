package de.kaviedes.thinkofyou3.dto;

public class RecoveryCodeResponse {
    private String recoveryCode;
    private boolean recoveryEmailSent;

    public RecoveryCodeResponse() {
    }

    public RecoveryCodeResponse(String recoveryCode, boolean recoveryEmailSent) {
        this.recoveryCode = recoveryCode;
        this.recoveryEmailSent = recoveryEmailSent;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }

    public void setRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public boolean isRecoveryEmailSent() {
        return recoveryEmailSent;
    }

    public void setRecoveryEmailSent(boolean recoveryEmailSent) {
        this.recoveryEmailSent = recoveryEmailSent;
    }
}
