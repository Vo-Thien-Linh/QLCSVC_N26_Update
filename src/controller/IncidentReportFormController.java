package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.UserSession;
import repository.IncidentRepository;
import utils.ScannerUtils;

import java.time.LocalDateTime;

public class IncidentReportFormController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateIncident;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    private IncidentRepository incidentRepository = new IncidentRepository();
    private String roomNumber; // Thay: roomId thành roomNumber

    @FXML
    private void initialize() {
        System.out.println("Initializing IncidentReportFormController...");
        btnSubmit.setOnAction(event -> handleSubmit());
        btnCancel.setOnAction(event -> handleCancel());
    }

    public void setRoomNumber(String roomNumber) { // Thay: setRoomId thành setRoomNumber
        this.roomNumber = roomNumber; // Thay: roomId thành roomNumber
        System.out.println("Set roomNumber: " + roomNumber); // Thay: roomId thành roomNumber
        if (roomNumber != null) {
            txtTitle.setText("Sự cố phòng: " + roomNumber); // Thay: roomId thành roomNumber
            dateIncident.setValue(LocalDateTime.now().toLocalDate());
        }
    }

    @FXML
    private void handleSubmit() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        LocalDateTime reportDate = dateIncident.getValue() != null ? dateIncident.getValue().atStartOfDay() : null;

        System.out.println("Submitting report - Title: " + title + ", Description: " + description + ", Date: " + reportDate);

        if (title.isEmpty() || description.isEmpty() || reportDate == null) {
            ScannerUtils.showError("Lỗi", "Vui lòng điền đầy đủ tiêu đề, mô tả và ngày xảy ra!");
            return;
        }

        String reportedBy = UserSession.getUserId();
        if (reportedBy == null || reportedBy.isEmpty()) {
            System.out.println("User ID is null or empty, using default userId: MTL0001");
            reportedBy = "MTL0001"; // Giả lập userId
        }
        System.out.println("Reported by: " + reportedBy);

        boolean success = incidentRepository.createIncident(reportedBy, null, roomNumber, description, reportDate, "SENT"); // Thay: roomId thành roomNumber
        System.out.println("Create incident result: " + success);
        if (success) {
            ScannerUtils.showInfo("Thành công", "Báo cáo sự cố đã được gửi!");
            handleCancel();
            btnSubmit.getScene().getWindow().hide();
        } else {
            ScannerUtils.showError("Lỗi", "Không thể gửi báo cáo sự cố!");
        }
    }

    @FXML
    private void handleCancel() {
        txtTitle.clear();
        txtDescription.clear();
        dateIncident.setValue(null);
        roomNumber = null; // Thay: roomId thành roomNumber
        System.out.println("Cancelled report form");
        btnCancel.getScene().getWindow().hide();
    }
}