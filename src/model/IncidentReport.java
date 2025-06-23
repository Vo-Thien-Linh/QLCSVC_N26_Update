package model;

import java.time.LocalDateTime;

public class IncidentReport {
    private String idReport;
    private String handledBy;
    private String roomNumber;
    private LocalDateTime reportDate;
    private String description;
    private IncidentStatus status;
    private LocalDateTime borrowDate;
    private String borrowerName;
    private String note;
    private Integer startPeriod;
    private Integer endPeriod;

    public IncidentReport(String idReport, String handledBy, String roomNumber, LocalDateTime reportDate, String description, IncidentStatus status) { // Thay: roomId thành roomNumber
        this.idReport = idReport;
        this.handledBy = handledBy;
        this.roomNumber = roomNumber; // Thay: roomId thành roomNumber
        this.reportDate = reportDate;
        this.description = description;
        this.status = status;
    }

    public String getIdReport() { return idReport; }
    public void setIdReport(String idReport) { this.idReport = idReport; }
    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
    public String getRoomNumber() { return roomNumber; } // Thay: getRoomId thành getRoomNumber
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; } // Thay: setRoomId thành setRoomNumber
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : null;
    }
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getStartPeriod() { return startPeriod; } // Thêm: Getter
    public void setStartPeriod(Integer startPeriod) { this.startPeriod = startPeriod; } // Thêm: Setter
    public Integer getEndPeriod() { return endPeriod; } // Thêm: Getter
    public void setEndPeriod(Integer endPeriod) { this.endPeriod = endPeriod; } // Thêm: Setter
}