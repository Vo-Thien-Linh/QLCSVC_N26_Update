package repository;

import config.DatabaseConnection;
import model.Room;
import model.RoomStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerRoomRepository {

    public static ArrayList<String> getAllRoomTypeNames() {
        ArrayList<String> roomTypes = new ArrayList<>();
        String query = "SELECT type_name FROM room_types";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomTypes.add(rs.getString("type_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomTypes;
    }

    public static int getRoomTypeIdByName(String typeName) {
        String query = "SELECT id FROM room_types WHERE type_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, typeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean create(Room room) {
        String query = "INSERT INTO room (room_id, status, room_number, seating_capacity, room_type_id, location, deleted) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int roomTypeId = getRoomTypeIdByName(room.getRoom_type());
        if (roomTypeId == -1) {
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, room.getId());
            stmt.setString(2, room.getStatus().name());
            stmt.setString(3, room.getRoomNumber());
            stmt.setInt(4, room.getSeatingCapacity());
            stmt.setInt(5, roomTypeId);
            stmt.setString(6, room.getLocation());
            stmt.setBoolean(7, false);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Room findById(String roomId) {
        String query = "SELECT r.*, rt.type_name FROM room r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.room_id = ? AND r.deleted = false";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("room_id");
                String statusStr = rs.getString("status");
                String roomNumber = rs.getString("room_number");
                int seatingCapacity = rs.getInt("seating_capacity");
                String location = rs.getString("location");
                String roomType = rs.getString("type_name");

                RoomStatus status = RoomStatus.valueOf(statusStr);
                return new Room(id, status, roomNumber, seatingCapacity, roomType, location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean edit(Room room) {
        String query = "UPDATE room SET status = ?, room_number = ?, seating_capacity = ?, room_type_id = ?, location = ? WHERE room_id = ? AND deleted = false";
        int roomTypeId = getRoomTypeIdByName(room.getRoom_type());
        if (roomTypeId == -1) {
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, room.getStatus().name());
            stmt.setString(2, room.getRoomNumber());
            stmt.setInt(3, room.getSeatingCapacity());
            stmt.setInt(4, roomTypeId);
            stmt.setString(5, room.getLocation());
            stmt.setString(6, room.getId());
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(String roomId) {
        String query = "UPDATE room SET deleted = true WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomId);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Room DataRoom(String roomId) {
        return findById(roomId);
    }

    public static List<Room> filterAndSearch(String status, String keyword, int limitItem, int skip) {
        String sql = "SELECT r.*, rt.type_name FROM room r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.deleted = false";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.equals("Tất cả")) {
            sql += " AND r.status = ?";
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND (LOWER(r.room_id) LIKE ? OR LOWER(r.room_number) LIKE ? OR LOWER(r.location) LIKE ?)";
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }

        sql += " ORDER BY r.room_id DESC LIMIT ? OFFSET ?";
        params.add(limitItem);
        params.add(skip);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            List<Room> rooms = new ArrayList<>();
            while (rs.next()) {
                String id = rs.getString("room_id");
                String statusStr = rs.getString("status");
                String roomNumber = rs.getString("room_number");
                int seatingCapacity = rs.getInt("seating_capacity");
                String location = rs.getString("location");
                String roomType = rs.getString("type_name");

                RoomStatus roomStatus = RoomStatus.valueOf(statusStr);
                rooms.add(new Room(id, roomStatus, roomNumber, seatingCapacity, roomType, location));
            }
            return rooms;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int countRooms(String status, String keyword) {
        String sql = "SELECT COUNT(*) FROM room WHERE deleted = false";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.equals("Tất cả")) {
            sql += " AND status = ?";
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND (LOWER(room_id) LIKE ? OR LOWER(room_number) LIKE ? OR LOWER(location) LIKE ?)";
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
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

    public static boolean changeStatus(List<String> ids, String status) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "UPDATE room SET";
        boolean isDelete = status.equals("Xóa");
        if (isDelete) {
            sql += " deleted = true";
        } else {
            sql += " status = ?";
        }
        sql += " WHERE room_id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            if (!isDelete) {
                stmt.setString(index++, status);
            }
            for (String id : ids) {
                stmt.setString(index++, id);
            }
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}