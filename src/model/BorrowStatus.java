package model;

public enum BorrowStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED ("Hủy bỏ"),
    REJECTED("Bị từ chối");

    private final String label;

    BorrowStatus(String label) {
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
