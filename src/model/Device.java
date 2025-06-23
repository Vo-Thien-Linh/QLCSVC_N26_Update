package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Device extends Facilities {
    private String thumbnail;
    private String deviceName;
    private String deviceType;
    private LocalDate purchaseDate;
    private String supplier;
    private BigDecimal price;
    private DeviceStatus status;
    private Room room;
    private int quantity;
    private int availableQuantity;
    private boolean deleted;
    private boolean selected;
    private String maintainedBy;
//    private final BooleanProperty selectedForBorrow = new SimpleBooleanProperty(false);
//    private Integer quantityToBorrow = 0;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Device(String id, String deviceName, DeviceStatus status) {
        super(id, null, null);
        this.deviceName = deviceName;
        this.status = status;
    }

    public Device(String id, String deviceName, int availableQuantity) {
        super(id, null, null);
        this.deviceName = deviceName;
        this.availableQuantity = availableQuantity;
    }

    public Device(String id, String thumbnail, String deviceName, String deviceType, LocalDate purchaseDate, String supplier, BigDecimal price, DeviceStatus status, Room room, int quantity, int availableQuantity) {
        super(id, null, null);
        this.thumbnail = thumbnail;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.purchaseDate = purchaseDate;
        this.supplier = supplier;
        this.price = price;
        this.status = status;
        this.room = room;
        this.availableQuantity = availableQuantity;
        this.quantity = quantity;
    }

    public void updateDevice(String deviceType, int quantity) {
        this.deviceType = deviceType;
        this.quantity = quantity;
        setUpdatedAt(LocalDate.now());
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

//    public boolean isSelectedForBorrow() {
//        return selectedForBorrow.get();
//    }
//
//    public void setSelectedForBorrow(boolean selected) {
//        selectedForBorrow.set(selected);
//    }
//
//    public BooleanProperty selectedForBorrowProperty() {
//        return selectedForBorrow;
//    }
//
//    public Integer getQuantityToBorrow() {
//        return quantityToBorrow;
//    }
//
//    public void setQuantityToBorrow(Integer quantityToBorrow) {
//        this.quantityToBorrow = quantityToBorrow;
//    }

    @Override
    public void informationDisplay() {
        System.out.println("Device ID: " + getId());
        System.out.println("Status: " + getStatus());
        System.out.println("Device Name: " + deviceName);
        System.out.println("Device Type: " + deviceType);
        System.out.println("Quantity: " + quantity);
        System.out.println("Room Number: " + room.getRoomNumber());
        System.out.println("Purchase Date: " + purchaseDate);
        System.out.println("Supplier: " + supplier);
        System.out.println("Value: " + price);
        System.out.println("Created At: " + getCreatedAt());
        System.out.println("Updated At: " + getUpdatedAt());
    }
    public String getMaintainedBy() {
        return maintainedBy;
    }

    public void setMaintainedBy(String maintainedBy) {
        this.maintainedBy = maintainedBy;
    }
}

