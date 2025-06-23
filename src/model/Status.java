package model;

public enum Status {
    ACTIVE("Hoạt động"),
    INACTIVE("Dừng hoạt động"),
    MAINTENANCE("Bảo trì");
    private final String label;

    Status(String label) {
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
