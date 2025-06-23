package repository;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class IncidentRepository {

    public boolean createIncident(String reportedBy, String deviceId, String roomNumber, String description, LocalDateTime reportDate, String status) {
        String sql = "INSERT INTO incident (reported_by, device_id, room_id, description, report_date, handled_by, status) " +
                "VALUES (?, ?, (SELECT room_id FROM room WHERE room_number = ?), ?, ?, NULL, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportedBy);
            stmt.setString(2, deviceId != null ? deviceId : null);
            stmt.setString(3, roomNumber);
            stmt.setString(4, description);
            stmt.setObject(5, reportDate);
            stmt.setString(6, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createIncidentWithDescription(String reportedBy, String deviceId, String roomNumber, String description, LocalDateTime reportDate, String status) { // Loại bỏ tham số description dư thừa
        String sql = "INSERT INTO incident (reported_by, device_id, room_id, description, report_date, handled_by, status) " +
                "VALUES (?, ?, (SELECT room_id FROM room WHERE room_number = ?), ?, ?, NULL, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reportedBy);
            stmt.setString(2, deviceId != null ? deviceId : null);
            stmt.setString(3, roomNumber);
            stmt.setString(4, description);
            stmt.setObject(5, reportDate);
            stmt.setString(6, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createIncidentWithBorrowInfo(String reportedBy, String deviceId, String roomNumber, String description, LocalDateTime reportDate, String status, LocalDateTime borrowDate, String borrowerName, Integer startPeriod, Integer endPeriod) { // Loại bỏ tham số description dư thừa
        String sqlIncident = "INSERT INTO incident (reported_by, device_id, room_id, description, report_date, handled_by, status) " +
                "VALUES (?, ?, (SELECT room_id FROM room WHERE room_number = ?), ?, ?, NULL, ?)";
        String sqlBorrow = "INSERT INTO borrow_room (room_id, borrower_id, borrow_date, start_period, end_period, status, created_at) " +
                "VALUES ((SELECT room_id FROM room WHERE room_number = ?), ?, ?, ?, ?, 'APPROVED', ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtIncident = conn.prepareStatement(sqlIncident)) {
                stmtIncident.setString(1, reportedBy);
                stmtIncident.setString(2, deviceId != null ? deviceId : null);
                stmtIncident.setString(3, roomNumber);
                stmtIncident.setString(4, description);
                stmtIncident.setObject(5, reportDate);
                stmtIncident.setString(6, status);
                int rowsAffected = stmtIncident.executeUpdate();
                if (rowsAffected > 0) {
                    try (PreparedStatement stmtBorrow = conn.prepareStatement(sqlBorrow)) {
                        stmtBorrow.setString(1, roomNumber);
                        stmtBorrow.setString(2, reportedBy); // Giả sử reportedBy là borrower_id
                        stmtBorrow.setObject(3, borrowDate);
                        stmtBorrow.setInt(4, startPeriod);
                        stmtBorrow.setInt(5, endPeriod);
                        stmtBorrow.setObject(6, LocalDateTime.now());
                        stmtBorrow.executeUpdate();
                        conn.commit();
                        return true;
                    }
                }
                conn.rollback();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getLastInsertId() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "INC" + String.format("%03d", rs.getInt(1));
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}