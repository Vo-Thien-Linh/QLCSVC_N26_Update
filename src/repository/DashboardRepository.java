package repository;

import config.DatabaseConnection;
import model.Device;
import model.DeviceStatus;
import model.Room;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {
    public int countDevices() {
        String sql = "SELECT SUM(quantity) AS total_quantity FROM devices WHERE deleted = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_quantity");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int countDevicesMaintenance() {
        String sql = "SELECT SUM(quantity) AS total_quantity FROM devices WHERE status = 'BROKEN' OR status = 'UNDER_MAINTENANCE' AND deleted = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_quantity");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int countDevicesUnavailable() {
        String sql = "SELECT SUM(quantity) AS total_quantity FROM devices WHERE status = 'UNAVAILABLE' AND deleted = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_quantity");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int countDevices(String status, String keyword) {
        String sql = "SELECT COUNT(*) FROM devices WHERE deleted = false";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.equals("Tất cả")) {
            sql += " AND status = ?";
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(device_name) LIKE ?";
            params.add("%" + keyword.toLowerCase() + "%");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static Room findById(Connection conn, String roomId) throws SQLException {
        String query = "SELECT room_number FROM room WHERE room_id = ? AND deleted = false";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, roomId);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new Room(roomId, rs.getString("room_number"), null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Device> filterAndSearch(String roomNumber, String keyword, int limitItem, int skip) {
        String sql = """
            SELECT d.*, dt.type_name
            FROM devices d
            JOIN room r ON d.room_id = r.room_id
            JOIN device_types dt ON dt.id = d.device_type_id
            WHERE d.deleted = FALSE AND r.deleted = FALSE
        """;
        List<Object> params = new ArrayList<>();

//      // Lọc theo tên phòng
        if (roomNumber != null && !roomNumber.equalsIgnoreCase("Tất cả")) {
            sql += " AND LOWER(r.room_number) = ?";
            params.add(roomNumber.toLowerCase());
        }

//        Tìm kiếm
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND (LOWER(d.device_name) LIKE ? OR LOWER(dt.type_name) LIKE ? OR LOWER(d.id) LIKE ?)";
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }

        sql += " ORDER BY d.id DESC";

//        Phân trang
        sql += " LIMIT ? OFFSET ?";
        params.add(limitItem);
        params.add(skip);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet result = stmt.executeQuery();
            List<Device> list = new ArrayList<>();
            while (result.next()) {
                String deviceId = result.getString("id");
                String thumbnail = result.getString("thumbnail");
                String deviceName = result.getString("device_name");
                String deviceType = result.getString("type_name");
                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
                String supplier = result.getString("supplier");
                BigDecimal price = result.getBigDecimal("price");
                String statusStr = result.getString("status");
                String roomId = result.getString("room_id");
                int quantity = result.getInt("quantity");
                int availableQuantity = result.getInt("available_quantity");

                DeviceStatus deviceStatus = DeviceStatus.valueOf(statusStr);
                Room room = findById(conn, roomId);

                list.add(new Device(deviceId, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, deviceStatus, room, quantity, availableQuantity));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Map<String, Object>> graphDataDump() {
        String sql = "SELECT device_name, SUM(quantity) AS total_quantity FROM devices WHERE deleted = false GROUP BY device_name";
        List<Map<String, Object>> data = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("device_name", rs.getString("device_name"));
                map.put("total_quantity", rs.getInt("total_quantity"));
                data.add(map);
            }

            return data;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getRoomAll(){
        String sql = "SELECT room_number FROM room WHERE deleted = false";
        List<String> listRooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                String roomNumber = rs.getString("room_number");
                listRooms.add(roomNumber);
            }

            return listRooms;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
