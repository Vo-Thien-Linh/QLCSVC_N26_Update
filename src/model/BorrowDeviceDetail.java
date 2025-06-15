package model;

public class BorrowDeviceDetail {
    private int id;
    private int borrowDeviceId;
    private Device device;
    private int quantity;

    public BorrowDeviceDetail() {}

    public BorrowDeviceDetail(int id, int borrowDeviceId, Device device, int quantity) {
        this.id = id;
        this.borrowDeviceId = borrowDeviceId;
        this.device = device;
        this.quantity = quantity;
    }

    public void setId(int id) {this.id = id;}

    public int getId() {
        return id;
    }

    public void setBorrowDeviceId(int borrowDeviceId) {}

    public int getBorrowDeviceId() {
        return borrowDeviceId;
    }

    public void setDevice(Device device) {this.device = device;}

    public Device getDevice() {
        return device;
    }

    public void setQuantity(int quantity) {this.quantity = quantity;}

    public int getQuantity() {
        return quantity;
    }
}

