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

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ResolvedIncidentsController implements Initializable {

    @FXML
    private TableView<IncidentReport> tblResolvedIncidents;
    @FXML
    private TableColumn<IncidentReport, String> colIdReport;
    @FXML
    private TableColumn<IncidentReport, String> colRoomNumber; // Thay: colRoomNumber đã đúng, giữ nguyên
    @FXML
    private TableColumn<IncidentReport, LocalDateTime> colReportDate;
    @FXML
    private TableColumn<IncidentReport, String> colNote;
    @FXML
    private TableColumn<IncidentReport, String> colHandledBy; // Hiển thị tên nhân viên
    @FXML
    private TableColumn<IncidentReport, String> colStatus;

    private ObservableList<IncidentReport> resolvedIncidentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Thiết lập các cột với lambda để sử dụng getter trực tiếp
        colIdReport.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIdReport()));
        colRoomNumber.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : "N/A")); // Thay: Sử dụng getRoomNumber
        colReportDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReportDate()));
        colNote.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));
        colHandledBy.setCellValueFactory(cellData -> {
            String handledById = cellData.getValue().getHandledBy();
            return new SimpleStringProperty(handledById != null ? getFullNameFromUserId(handledById) : "Chưa xử lý");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatusDisplayName()));

        // Tải dữ liệu
        loadResolvedIncidentData();
    }

    private void loadResolvedIncidentData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT i.id_report, i.reported_by, r.room_number, i.description, i.report_date, i.handled_by, i.status, i.note " + // Thay: room_id thành room_number
                     "FROM incident i " +
                     "JOIN room r ON i.room_id = r.room_id " + // Giữ join để lấy room_number
                     "WHERE i.status = 'RESOLVED'")) {

            resolvedIncidentList.clear();
            while (rs.next()) {
                IncidentReport incident = new IncidentReport(
                        rs.getString("id_report"),
                        rs.getString("handled_by"), // Lưu mã nhân viên
                        rs.getString("room_number"), // Thay: room_id thành room_number
                        rs.getObject("report_date", LocalDateTime.class),
                        rs.getString("description"),
                        IncidentStatus.valueOf(rs.getString("status"))
                );
                incident.setNote(rs.getString("note"));
                resolvedIncidentList.add(incident);
            }
            tblResolvedIncidents.setItems(resolvedIncidentList);

        } catch (SQLException e) {
            e.printStackTrace();
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
}