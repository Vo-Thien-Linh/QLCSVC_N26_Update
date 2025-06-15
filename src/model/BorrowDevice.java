package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowDevice {
    private int id;
    private String userId;
    private LocalDate borrowDate;
    private int startPeriod;
    private int endPeriod;
    private BorrowStatus borrowStatus; // PENDING, APPROVED, etc.
    private LocalDateTime createdAt;
    private Integer borrowRoomId; // nullable
    private String note;

    public BorrowDevice() {}

    public BorrowDevice(int id, String userId, LocalDate borrowDate, int startPeriod, int endPeriod,
                        BorrowStatus borrowStatus, LocalDateTime createdAt, Integer borrowRoomId, String note) {
        this.id = id;
        this.userId = userId;
        this.borrowDate = borrowDate;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.borrowStatus = borrowStatus;
        this.createdAt = createdAt;
        this.borrowRoomId = borrowRoomId;
        this.note = note;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
