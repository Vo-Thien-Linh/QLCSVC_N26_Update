package repository;

import config.DatabaseConnection;
import model.DeviceStatus;
import model.Room;
import model.UsageStat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticalRepository {
    public List<Map<String, Object>> graphDataDump() {
        String sql = "SELECT room.room_number, SUM(devices.quantity) AS total_quantity FROM devices JOIN room ON devices.room_id = room.room_id  WHERE devices.deleted = false GROUP BY devices.room_id";
        List<Map<String, Object>> data = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("room_number", rs.getString("room_number"));
                map.put("total_quantity", rs.getInt("total_quantity"));
                data.add(map);
            }

            return data;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static ArrayList<Room> getAllRooms(){
        ArrayList<Room> rooms = new ArrayList<>();
        String query = "SELECT room_id, room_number FROM room WHERE status = 'AVAILABLE'";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet result = stmt.executeQuery();
            while(result.next()) {
                String roomId = result.getString("room_id");
                String roomNumber = result.getString("room_number");
                rooms.add(new Room(roomId, roomNumber));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

//    public List<UsageStat> getUsageStats() {
//        List<UsageStat> stats = new ArrayList<>();
//        String sql = """
//            SELECT d.device_name,
//                   DATE_FORMAT(u.used_at, '%Y-%m') AS usage_month,
//                   COUNT(*) AS usage_count
//            FROM usage_logs u
//            JOIN devices d ON u.device_id = d.id
//            GROUP BY d.device_name, usage_month
//            ORDER BY usage_month, d.device_name
//        """;
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                stats.add(new UsageStat(
//                        rs.getString("device_name"),
//                        rs.getString("usage_month"),
//                        rs.getInt("usage_count")
//                ));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return stats;
//    }

    public List<UsageStat> getUsageStatsByYear(int year, String roomNumber) {
        List<UsageStat> stats = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                    SELECT d.device_name,
                           DATE_FORMAT(u.used_at, '%Y-%m') AS usage_month,
                           COUNT(*) AS usage_count
                    FROM usage_logs u
                    JOIN devices d ON u.device_id = d.id
                    JOIN room r ON d.room_id = r.room_id
                    WHERE YEAR(u.used_at) = ?
                """);

        // Nếu có lọc theo phòng, thêm điều kiện
        if (roomNumber != null && !roomNumber.equalsIgnoreCase("Tất cả")) {
            sql.append(" AND r.room_number = ? ");
        }

        sql.append(" GROUP BY d.device_name, usage_month ORDER BY usage_month, d.device_name ");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, year);

            if (roomNumber != null && !roomNumber.equalsIgnoreCase("Tất cả")) {
                stmt.setString(2, roomNumber);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(new UsageStat(
                            rs.getString("device_name"),
                            rs.getString("usage_month"),
                            rs.getInt("usage_count")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public Map<DeviceStatus, Integer> getDeviceStatusCount() {
        Map<DeviceStatus, Integer> result = new HashMap<>();
        String query = "SELECT status, COUNT(*) as count FROM devices GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.put(DeviceStatus.valueOf(rs.getString("status")), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
