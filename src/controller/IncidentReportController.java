package controller;

import config.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.IncidentReport;
import model.IncidentStatus;
import model.UserSession;
import utils.ScannerUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncidentReportController {

    @FXML private TableView<IncidentReport> tblBorrowedRooms;
    @FXML private TableColumn<IncidentReport, String> colIdReport;
    @FXML private TableColumn<IncidentReport, String> colRoomNumber;
    @FXML private TableColumn<IncidentReport, String> colBorrowDate;
    @FXML private TableColumn<IncidentReport, String> colBorrower;
    @FXML private TableColumn<IncidentReport, String> colDescription;
    @FXML private TableColumn<IncidentReport, String> colStartPeriod;
    @FXML private TableColumn<IncidentReport, String> colEndPeriod;
    @FXML private TableColumn<IncidentReport, String> colAction;
    @FXML private Button btnViewReports;

    @FXML
    private void initialize() {
        System.out.println("Initializing IncidentReportController...");
        setupTableColumns();
        loadIncidentReportsForBorrowedRooms();
        btnViewReports.setOnAction(event -> showReportHistory());
        tblBorrowedRooms.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                IncidentReport selected = tblBorrowedRooms.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getRoomNumber() != null) {
                    openReportForm(selected.getRoomNumber());
                }
            }
        });
    }

    private void setupTableColumns() {
        colIdReport.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdReport() != null ? cellData.getValue().getIdReport() : "Chưa báo cáo"));
        colRoomNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : ""));
        colBorrowDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBorrowDate() != null ? cellData.getValue().getBorrowDate().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
        colBorrower.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBorrowerName() != null ? cellData.getValue().getBorrowerName() : ""));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "Chưa có mô tả"));
        colStartPeriod.setCellValueFactory(cellData -> {
            Integer start = cellData.getValue().getStartPeriod();
            return new SimpleStringProperty(start != null ? start.toString() : "N/A");
        });
        colStartPeriod.setPrefWidth(100);
        colEndPeriod.setCellValueFactory(cellData -> {
            Integer end = cellData.getValue().getEndPeriod();
            return new SimpleStringProperty(end != null ? end.toString() : "N/A");
        });
        colEndPeriod.setPrefWidth(100);

        colAction.setCellFactory(tc -> new TableCell<IncidentReport, String>() {
            private final Button btnReport = new Button("📝");

            {
                btnReport.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 10px; -fx-cursor: hand;");
                btnReport.setTooltip(new Tooltip("Báo cáo sự cố"));
                btnReport.setOnMouseEntered(event -> btnReport.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 10px; -fx-cursor: hand;"));
                btnReport.setOnMouseExited(event -> btnReport.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 10px; -fx-cursor: hand;"));
                btnReport.setOnAction(event -> {
                    IncidentReport report = getTableView().getItems().get(getIndex());
                    if (report != null && report.getRoomNumber() != null) {
                        openReportForm(report.getRoomNumber());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnReport);
            }
        });
    }

    private void loadIncidentReportsForBorrowedRooms() {
        List<IncidentReport> reports = new ArrayList<>();
        String userId = UserSession.getUserId();
        if (userId == null || userId.isEmpty()) {
            System.out.println("User ID is null or empty, using default userId: MTL0001");
            userId = "MTL0001";
        }

        String sql = "SELECT r.room_id, r.room_number, br.borrow_date, u.fullname AS borrower_name, i.id_report, i.description, " +
                "br.start_period, br.end_period " +
                "FROM room r " +
                "JOIN borrow_room br ON r.room_id = br.room_id " +
                "LEFT JOIN incident i ON r.room_id = i.room_id AND i.reported_by = ? AND i.status = 'SENT' " +
                "LEFT JOIN users u ON br.borrower_id = u.user_id " +
                "WHERE br.borrower_id = ? AND br.status = 'APPROVED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String roomNumber = rs.getString("room_number");
                IncidentReport report = new IncidentReport(
                        rs.getString("id_report"),
                        null,
                        roomNumber,
                        null,
                        rs.getString("description"),
                        null
                );
                report.setBorrowDate(rs.getObject("borrow_date", LocalDateTime.class));
                String borrowerName = rs.getString("borrower_name");
                report.setBorrowerName(borrowerName != null ? borrowerName : userId);
                report.setStartPeriod(rs.getInt("start_period"));
                report.setEndPeriod(rs.getInt("end_period"));
                reports.add(report);
            }
            System.out.println("Loaded " + reports.size() + " borrowed rooms for user: " + userId);
        } catch (SQLException e) {
            ScannerUtils.showError("Lỗi cơ sở dữ liệu", "Đã xảy ra lỗi khi tải dữ liệu: " + e.getMessage());
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        tblBorrowedRooms.setItems(FXCollections.observableArrayList(reports));
    }

    private void showReportHistory() {
        List<IncidentReport> reports = new ArrayList<>();
        String userId = UserSession.getUserId();
        if (userId == null || userId.isEmpty()) {
            System.out.println("User ID is null or empty, using default userId: MTL0001");
            userId = "MTL0001";
        }

        String sql = "SELECT i.id_report, r.room_number, i.report_date, i.description, i.status " +
                "FROM incident i " +
                "JOIN room r ON i.room_id = r.room_id " +
                "WHERE i.reported_by = ? " +
                "ORDER BY i.report_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                reports.add(new IncidentReport(
                        rs.getString("id_report"),
                        null,
                        rs.getString("room_number"),
                        rs.getObject("report_date", LocalDateTime.class),
                        rs.getString("description"),
                        IncidentStatus.valueOf(rs.getString("status"))
                ));
            }
            System.out.println("Loaded " + reports.size() + " reports for user: " + userId);
        } catch (SQLException e) {
            ScannerUtils.showError("Lỗi cơ sở dữ liệu", "Đã xảy ra lỗi khi tải dữ liệu: " + e.getMessage());
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        if (reports.isEmpty()) {
            ScannerUtils.showInfo("Thông báo", "Bạn chưa gửi bất kỳ đơn nào!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Maintenance/ReportHistory.fxml"));
            Parent root = loader.load();
            ReportHistoryController controller = loader.getController();
            controller.setReports(reports);
            Stage stage = new Stage();
            stage.setTitle("Lịch sử báo cáo");
            stage.setScene(new Scene(root, 1350, 400));
            stage.show();
        } catch (IOException e) {
            ScannerUtils.showError("Lỗi", "Không thể mở lịch sử báo cáo!");
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openReportForm(String roomNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Maintenance/Form.fxml"));
            Parent root = loader.load();
            IncidentReportFormController controller = loader.getController();
            controller.setRoomNumber(roomNumber);
            Stage stage = new Stage();
            stage.setTitle("Báo cáo sự cố");
            stage.setScene(new Scene(root, 500, 400));
            stage.show();
        } catch (IOException e) {
            ScannerUtils.showError("Lỗi", "Không thể mở form báo cáo!");
            System.err.println("Error opening report form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}