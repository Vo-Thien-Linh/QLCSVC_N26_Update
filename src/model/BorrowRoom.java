package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowRoom {
    private int id;
    private String roomId;
    private String roomNumber;
    private User borrower;
    private LocalDate borrowDate;
    private LocalDateTime createdAt;
    private int startPeriod;
    private int endPeriod;
    private String borrowReason;
    private String rejectReason;
    private BorrowStatus status;
    private List<BorrowDeviceDetail> devices = new ArrayList<>();

    public BorrowRoom() {}

    public BorrowRoom(int id, String roomId, String roomNumber, User borrower, LocalDate borrowDate, int startPeriod, int endPeriod, String borrowReason, String rejectReason, BorrowStatus status, List<BorrowDeviceDetail> devices) {
        this.id = id;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.borrower = borrower;
        this.borrowDate = borrowDate;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.borrowReason = borrowReason;
        this.rejectReason = rejectReason;
        this.status = status;
        this.devices = devices;
    }

    public void setId(int id){this.id = id;}

    public int getId() {
        return id;
    }

    public void setRoomId(String roomId){this.roomId = roomId;}

    public String getRoomId() {
        return roomId;
    }

    public void setRoomNumber(String roomNumber){this.roomNumber = roomNumber;}

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setBorrower(User borrower){this.borrower = borrower;}

    public User getBorrower() {
        return borrower;
    }

    public void setBorrowDate(LocalDate borrowDate){this.borrowDate = borrowDate;}

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStartPeriod(int startPeriod){this.startPeriod = startPeriod;}

    public int getstartPeriod () {
        return startPeriod;
    }

    public void setEndPeriod(int endPeriod){this.endPeriod = endPeriod;}

    public int getEndPeriod() {
        return endPeriod;
    }

    public void setBorrowReason(String borrowReason){this.borrowReason = borrowReason;}

    public String getBorrowReason() {
        return borrowReason;
    }

    public void setRejectReason(String rejectReason){this.rejectReason = rejectReason;}

    public String getRejectReason() {
        return rejectReason;
    }

    public void setStatus(BorrowStatus status){this.status = status;}

    public BorrowStatus getStatus() {
        return status;
    }

    public void addDevice(BorrowDeviceDetail device) {
        this.devices.add(device);
    }

    public List<BorrowDeviceDetail> getBorrowDeviceDetail() {
        return devices;
    }
}

