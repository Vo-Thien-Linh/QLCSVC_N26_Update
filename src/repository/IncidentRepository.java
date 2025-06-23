package repository;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class IncidentRepository {

    public boolean createIncident(String reportedBy, String deviceId, String roomNumber, String description, LocalDateTime reportDate, String status) { // Thay: roomId thành roomNumber
        String sql = "INSERT INTO incident (reported_by, device_id, room_id, description, report_date, handled_by, status) " + // Giữ room_id trong SQL vì database dùng room_id
                "VALUES (?, ?, (SELECT room_id FROM room WHERE room_number = ?), ?, ?, NULL, ?)"; // Thay: Sử dụng subquery để lấy room_id từ room_number
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportedBy);
            stmt.setString(2, deviceId);
            stmt.setString(3, roomNumber); // Thay: roomId thành roomNumber
            stmt.setString(4, description);
            stmt.setObject(5, reportDate);
            stmt.setString(6, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm phương thức mới với tham số note
    public boolean createIncidentWithNote(String reportedBy, String deviceId, String roomNumber, String description, LocalDateTime reportDate, String status, String note) { // Thay: roomId thành roomNumber
        String sql = "INSERT INTO incident (reported_by, device_id, room_id, description, report_date, handled_by, status, note) " + // Giữ room_id trong SQL
                "VALUES (?, ?, (SELECT room_id FROM room WHERE room_number = ?), ?, ?, NULL, ?, ?)"; // Thay: Sử dụng subquery để lấy room_id từ room_number
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportedBy);
            stmt.setString(2, deviceId);
            stmt.setString(3, roomNumber); // Thay: roomId thành roomNumber
            stmt.setString(4, description);
            stmt.setObject(5, reportDate);
            stmt.setString(6, status);
            stmt.setString(7, note); // Thêm tham số note
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}