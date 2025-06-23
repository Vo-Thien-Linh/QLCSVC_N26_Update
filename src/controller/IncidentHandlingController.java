package controller;

import config.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.IncidentReport;
import model.IncidentStatus;
import model.UserSession;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class IncidentHandlingController implements Initializable {

    @FXML
    private TableView<IncidentReport> tblIncidents;
    @FXML
    private TableColumn<IncidentReport, String> colIdReport;
    @FXML
    private TableColumn<IncidentReport, String> colRoomNumber;
    @FXML
    private TableColumn<IncidentReport, LocalDateTime> colReportDate;
    @FXML
    private TableColumn<IncidentReport, String> colDescription;
    @FXML
    private TableColumn<IncidentReport, String> colHandledBy;
    @FXML
    private TableColumn<IncidentReport, String> colStatus;
    @FXML
    private Button btnConfirmRepair;
    @FXML
    private Label lblMessage;
    @FXML
    private Button btnViewHistory;

    private ObservableList<IncidentReport> incidentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadIncidentData();
        btnConfirmRepair.setOnAction(event -> confirmRepair());
        btnViewHistory.setOnAction(event -> showMaintenanceHistory());
    }

    private void setupTableColumns() {
        colIdReport.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdReport()));
        colRoomNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : "N/A"));
        colReportDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReportDate()));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "Chưa có mô tả"));
        colHandledBy.setCellValueFactory(cellData -> {
            String handledById = cellData.getValue().getHandledBy();
            return new SimpleStringProperty(handledById != null ? getFullNameFromUserId(handledById) : "Chưa xử lý");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatusDisplayName()));
    }

    private void loadIncidentData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT i.id_report, i.reported_by, r.room_number, i.description, i.report_date, i.handled_by, i.status " +
                     "FROM incident i " +
                     "JOIN room r ON i.room_id = r.room_id " +
                     "WHERE i.status = 'SENT'")) {

            incidentList.clear();
            while (rs.next()) {
                IncidentReport incident = new IncidentReport(
                        rs.getString("id_report"),
                        rs.getString("handled_by"),
                        rs.getString("room_number"),
                        rs.getObject("report_date", LocalDateTime.class),
                        rs.getString("description"),
                        IncidentStatus.valueOf(rs.getString("status"))
                );
                System.out.println("Loaded incident " + rs.getString("id_report") + ", Description: " + rs.getString("description"));
                incidentList.add(incident);
            }
            tblIncidents.setItems(incidentList);

        } catch (SQLException e) {
            e.printStackTrace();
            lblMessage.setText("Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private String getRoomNumberFromId(String roomId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT room_number FROM room WHERE room_id = ?")) {
            pstmt.setString(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("room_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getFullNameFromUserId(String userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT fullname FROM users WHERE user_id = ?")) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("fullname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void confirmRepair() {
        IncidentReport selectedIncident = tblIncidents.getSelectionModel().getSelectedItem();
        if (selectedIncident != null) {
            if (selectedIncident.getStatus() == IncidentStatus.SENT) {
                String currentStaffId = UserSession.getUserId();
                if (currentStaffId == null || currentStaffId.trim().isEmpty()) {
                    lblMessage.setText("Vui lòng đăng nhập để xác nhận sửa!");
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("UPDATE incident SET status = ?, handled_by = ? WHERE id_report = ?")) {
                    pstmt.setString(1, IncidentStatus.RESOLVED.name());
                    pstmt.setString(2, currentStaffId);
                    pstmt.setString(3, selectedIncident.getIdReport());
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        selectedIncident.setStatus(IncidentStatus.RESOLVED);
                        selectedIncident.setHandledBy(currentStaffId);
                        tblIncidents.refresh();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thành công");
                        alert.setHeaderText(null);
                        alert.setContentText("Sự cố đã được xác nhận sửa thành công!");
                        alert.showAndWait();
                        loadIncidentData();
                    } else {
                        lblMessage.setText("Không thể cập nhật sự cố!");
                    }
                } catch (SQLException e) {
                    lblMessage.setText("Lỗi khi xác nhận sửa: " + e.getMessage());
                }
            } else {
                lblMessage.setText("Sự cố này đã được xử lý!");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một sự cố để xác nhận!");
            alert.showAndWait();
        }
    }

    private void showMaintenanceHistory() {
        String currentStaffId = UserSession.getUserId();
        if (currentStaffId == null || currentStaffId.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng đăng nhập để xem lịch sử!");
            alert.showAndWait();
            return;
        }

        ObservableList<IncidentReport> historyList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT i.id_report, r.room_number, i.report_date, i.handled_by, i.status, i.description " +
                             "FROM incident i " +
                             "JOIN room r ON i.room_id = r.room_id " +
                             "WHERE i.handled_by = ? AND i.status = 'RESOLVED'")) {
            pstmt.setString(1, currentStaffId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                IncidentReport incident = new IncidentReport(
                        rs.getString("id_report"),
                        rs.getString("handled_by"),
                        rs.getString("room_number"),
                        rs.getObject("report_date", LocalDateTime.class),
                        rs.getString("description"), // Thay note bằng description
                        IncidentStatus.valueOf(rs.getString("status"))
                );
                System.out.println("History incident " + rs.getString("id_report") + ", Description: " + rs.getString("description"));
                historyList.add(incident);
            }

            if (historyList.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Bạn chưa xử lý sự cố nào!");
                alert.showAndWait();
            } else {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Lịch sử xử lý của " + getFullNameFromUserId(currentStaffId));
                dialog.setHeaderText(null);

                TableView<IncidentReport> historyTable = new TableView<>();
                TableColumn<IncidentReport, String> histIdReport = new TableColumn<>("Mã báo cáo");
                histIdReport.setPrefWidth(120);
                histIdReport.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdReport()));
                TableColumn<IncidentReport, String> histRoomNumber = new TableColumn<>("Số phòng");
                histRoomNumber.setPrefWidth(130);
                histRoomNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : "N/A"));
                TableColumn<IncidentReport, LocalDateTime> histReportDate = new TableColumn<>("Ngày báo cáo");
                histReportDate.setPrefWidth(160);
                histReportDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReportDate()));
                TableColumn<IncidentReport, String> histDescription = new TableColumn<>("Mô tả"); // Thay note bằng description
                histDescription.setPrefWidth(350);
                histDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "Chưa có mô tả"));
                TableColumn<IncidentReport, String> histHandledBy = new TableColumn<>("Người xử lý");
                histHandledBy.setPrefWidth(150);
                histHandledBy.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHandledBy() != null ? getFullNameFromUserId(cellData.getValue().getHandledBy()) : "Chưa xử lý"));

                historyTable.getColumns().addAll(histIdReport, histRoomNumber, histReportDate, histDescription, histHandledBy);
                historyTable.setPrefWidth(910);
                historyTable.setPrefHeight(450);
                historyTable.setItems(historyList);

                dialog.getDialogPane().setContent(historyTable);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Lỗi khi tải lịch sử: " + e.getMessage());
            alert.showAndWait();
        }
    }
}