package model;

public enum IncidentStatus {
    SENT("Đã gửi"),
    RESOLVED("Đã xử lý");

    private final String displayName;

    IncidentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}