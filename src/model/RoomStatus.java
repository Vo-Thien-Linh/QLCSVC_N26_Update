package model;

public enum RoomStatus {
    AVAILABLE("Có sẵn"),
    OCCUPIED("Đang sử dụng"),
    MAINTENANCE("Bảo trì");

    private final String label;

    RoomStatus(String label) {
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

