package model;

public enum DeviceStatus {
    AVAILABLE("Có sẵn"),
    UNAVAILABLE("Không có sẵn"),
    UNDER_MAINTENANCE("Đang bảo trì"),
    BROKEN("Bị hỏng");

    private final String label;

    DeviceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

