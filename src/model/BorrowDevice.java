package model;

public class BorrowDevice {
    private int id;
    private int borrowRoomId;
    private String deviceId;
    private int quantity;

    public BorrowDevice(int id, int borrowRoomId, String deviceId, int quantity) {
        this.id = id;
        this.borrowRoomId = borrowRoomId;
        this.deviceId = deviceId;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getBorrowRoomId() {
        return borrowRoomId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getQuantity() {
        return quantity;
    }
}

