package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowRoom {
    private int id;
    private String roomId;
    private String roomNumber;
    private String borrowerId;
    private LocalDate borrowDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String borrowReason;
    private String rejectReason;
    private BorrowRoomStatus status;
    private List<BorrowDevice> devices; // mượn kèm

    public BorrowRoom(int id, String roomId, String roomNumber, String borrowerId, LocalDate borrowDate, LocalTime startTime, LocalTime endTime, String borrowReason, String rejectReason, BorrowRoomStatus status, List<BorrowDevice> devices) {
        this.id = id;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.borrowReason = borrowReason;
        this.rejectReason = rejectReason;
        this.status = status;
        this.devices = devices;
    }

    public int getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getBorrowReason() {
        return borrowReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public BorrowRoomStatus getStatus() {
        return status;
    }

    public void addDevice(BorrowDevice device) {
        this.devices.add(device);
    }

    public List<BorrowDevice> getBorrowDevices() {
        return devices;
    }
}

