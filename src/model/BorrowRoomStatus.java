package model;

public enum BorrowRoomStatus {
    PENDING("Đang duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED ("Hủy bỏ"),
    REJECTED("Bị từ chối");

    private final String label;

    BorrowRoomStatus(String label) {
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
