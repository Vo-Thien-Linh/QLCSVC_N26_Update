package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowDevice {
    private int id;
    private User borrower;
    private LocalDate borrowDate;
    private int startPeriod;
    private int endPeriod;
    private BorrowStatus borrowStatus;
    private LocalDateTime createdAt;
    private Integer borrowRoomId;
    private String borrowReason;// nullable
    private String rejectReason;
    private BorrowDeviceDetail borrowDeviceDetail;

    public BorrowDevice() {}

    public BorrowDevice(int id, User borrower, LocalDate borrowDate, int startPeriod, int endPeriod,
                        BorrowStatus borrowStatus, LocalDateTime createdAt, Integer borrowRoomId, String borrowReason, String rejectReason,  BorrowDeviceDetail borrowDeviceDetail) {
        this.id = id;
        this.borrower = borrower;
        this.borrowDate = borrowDate;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.borrowStatus = borrowStatus;
        this.createdAt = createdAt;
        this.borrowRoomId = borrowRoomId;
        this.borrowReason = borrowReason;
        this.rejectReason = rejectReason;
        this.borrowDeviceDetail = borrowDeviceDetail;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getBorrower() {
        return borrower;
    }

    public void setBorrower(User borrower) {
        this.borrower = borrower;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public int getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(int startPeriod) {
        this.startPeriod = startPeriod;
    }

    public int getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(int endPeriod) {
        this.endPeriod = endPeriod;
    }

    public BorrowStatus getBorrowStatus() {
        return borrowStatus;
    }

    public void setBorrowStatus(BorrowStatus borrowStatus) {
        this.borrowStatus = borrowStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getBorrowRoomId() {
        return borrowRoomId;
    }

    public void setBorrowRoomId(Integer borrowRoomId) {
        this.borrowRoomId = borrowRoomId;
    }

    public String getBorrowReason() {
        return borrowReason;
    }

    public String  getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public void setBorrowReason(String borrowReason) {
        this.borrowReason = borrowReason;
    }

    public BorrowDeviceDetail getBorrowDeviceDetail() {
        return borrowDeviceDetail;
    }

    public void setBorrowDeviceDetail(BorrowDeviceDetail borrowDeviceDetail) {
        this.borrowDeviceDetail = borrowDeviceDetail;
    }
}
