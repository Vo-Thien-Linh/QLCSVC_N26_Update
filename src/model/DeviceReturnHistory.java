package model;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public class DeviceReturnHistory {
    private int returnQuantity;
    private String conditionNote;
    private LocalDateTime returnTime;
    private BorrowDevice borrowDevice;

    public  DeviceReturnHistory() {}


    public DeviceReturnHistory(int returnQuantity, String conditionNote, LocalDateTime returnTime, BorrowDevice borrowDevice) {
        this.returnQuantity = returnQuantity;
        this.conditionNote = conditionNote;
        this.returnTime = returnTime;
        this.borrowDevice = borrowDevice;
    }

    public int getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(int returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    public void setConditionNote(String conditionNote) {
        this.conditionNote = conditionNote;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public BorrowDevice getBorrowDevice() {
        return borrowDevice;
    }

    public void setBorrowDevice(BorrowDevice borrowDevice) {
        this.borrowDevice = borrowDevice;
    }
}


