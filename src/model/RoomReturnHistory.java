package model;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public class RoomReturnHistory {
    private String returnNote;
    private int returnQuantity;
    private String conditionNote;
    private LocalDateTime returnTime;
    private BorrowRoom borrowRoom;

    public  RoomReturnHistory() {}


    public RoomReturnHistory(String returnNote, int returnQuantity, String conditionNote, LocalDateTime returnTime, BorrowRoom borrowRoom) {
        this.returnNote = returnNote;
        this.returnQuantity = returnQuantity;
        this.conditionNote = conditionNote;
        this.returnTime = returnTime;
        this.borrowRoom = borrowRoom;
    }

    public String getReturnNote() {
        return returnNote;
    }

    public void setReturnNote(String returnNote) {
        this.returnNote = returnNote;
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

    public BorrowRoom getBorrowRoom() {
        return borrowRoom;
    }

    public void setBorrowRoom(BorrowRoom borrowRoom) {
        this.borrowRoom = borrowRoom;
    }
}


