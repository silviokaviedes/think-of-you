package de.kaviedes.thinkofyou3.dto;

public class DashboardPreferenceDTO {
    private String mode;

    public DashboardPreferenceDTO() {
    }

    public DashboardPreferenceDTO(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
