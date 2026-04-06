package de.kaviedes.thinkofyou3.dto;

public class DeleteAccountRequest {
    private String currentPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
