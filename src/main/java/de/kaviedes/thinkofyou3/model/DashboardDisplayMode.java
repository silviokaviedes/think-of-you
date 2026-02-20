package de.kaviedes.thinkofyou3.model;

public enum DashboardDisplayMode {
    COUNTS("counts"),
    LAST_EVENT("last_event");

    private final String value;

    DashboardDisplayMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DashboardDisplayMode fromValue(String value) {
        if (value == null) {
            return COUNTS;
        }
        for (DashboardDisplayMode mode : DashboardDisplayMode.values()) {
            if (mode.getValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new RuntimeException("Unknown dashboard mode: " + value);
    }
}
