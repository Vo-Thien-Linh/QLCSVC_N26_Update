package repository;

import java.sql.Statement;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import config.DatabaseConnection;
import model.Device;
import model.DeviceStatus;
import model.Room;

public class ManagerDeviceRepository {

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

    public static Boolean create(Device device) {
        String query = "INSERT INTO devices (device_name, device_type, purchase_date, supplier, price, status, room_id, quantity, created_at, updated_at, thumbnail, is_borrowable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setString(1, device.getDeviceName());
            stmt.setString(2, device.getDeviceType());
            stmt.setDate(3, Date.valueOf(device.getPurchaseDate()));
            stmt.setString(4, device.getSupplier());
            stmt.setBigDecimal(5, device.getPrice());
            stmt.setString(6, device.getStatus().name());
            stmt.setString(7, device.getRoom().getId());
            stmt.setInt(8, device.getQuantity());
            stmt.setDate(9, Date.valueOf(device.getCreatedAt()));
            stmt.setDate(10, Date.valueOf(device.getUpdatedAt()));
            stmt.setString(11, device.getThumbnail());
            stmt.setBoolean(12, device.getAllow());

            int resultSet = stmt.executeUpdate();
            if(resultSet > 0) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public static Room findById(Connection conn, String roomId) throws SQLException {
        String query = "SELECT room_number FROM room WHERE room_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, roomId);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new Room(roomId, rs.getString("room_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Device DataDevice(String deviceId) {
        String query = "SELECT * FROM devices WHERE id = ? AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, deviceId);

            ResultSet result = stmt.executeQuery();
            if(result.next()) {
                String thumbnail = result.getString("thumbnail");
                String deviceName = result.getString("device_name");
                String deviceType = result.getString("device_type");
                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
                String supplier = result.getString("supplier");
                BigDecimal price = result.getBigDecimal("price");
                String statusStr = result.getString("status");
                String roomId = result.getString("room_id");
                int quantity = result.getInt("quantity");
                Boolean isAllow = result.getBoolean("is_borrowable");

                DeviceStatus status = DeviceStatus.valueOf(statusStr);

                Room room = findById(conn, roomId);

                return new Device(null, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, status, room, quantity, isAllow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean edit(Device device) {
        String query = "UPDATE devices SET device_name = ?, device_type = ?, purchase_date = ?, supplier = ?, price = ?, status = ?, room_id = ?, quantity = ?, updated_at = ?, thumbnail = ?, is_borrowable = ? WHERE id = ? AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, device.getDeviceName());
            stmt.setString(2, device.getDeviceType());
            stmt.setDate(3, Date.valueOf(device.getPurchaseDate()));
            stmt.setString(4, device.getSupplier());
            stmt.setBigDecimal(5, device.getPrice());
            stmt.setString(6, device.getStatus().name());
            stmt.setString(7, device.getRoom().getId());
            stmt.setInt(8, device.getQuantity());
            stmt.setDate(9, Date.valueOf(device.getUpdatedAt()));
            stmt.setString(10, device.getThumbnail());
            stmt.setBoolean(11, device.getAllow());
            stmt.setString(12, device.getId());

            int result = stmt.executeUpdate();
            if(result > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delete(String deviceId) {
        String query = "UPDATE devices SET deleted = true WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, deviceId);

            int result = stmt .executeUpdate();
            if(result > 0) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    //	Tìm kiếm người dùng
//    public List<Device> searchUsers(String[] keyword){
//        List<Device> listDevices = new ArrayList<>();
//        StringBuilder sql = new StringBuilder("SELECT * FROM devices WHERE deleted = false");
//        for (String kw : keyword) {
//            sql.append(" AND LOWER(device_name) LIKE ?");
//        }
//        try(Connection conn = DatabaseConnection.getConnection();
//            PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
//            for (int i = 0; i < keyword.length; i++) {
//                stmt.setString(i + 1, "%" + keyword[i] + "%");
//            }
//            ResultSet result = stmt.executeQuery();
//            while(result.next()) {
//                String deviceId = result.getString("id");
//                String thumbnail = result.getString("thumbnail");
//                String deviceName = result.getString("device_name");
//                String deviceType = result.getString("device_type");
//                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
//                String supplier = result.getString("supplier");
//                BigDecimal price = result.getBigDecimal("price");
//                String statusStr = result.getString("status");
//                String roomId = result.getString("room_id");
//                int quantity = result.getInt("quantity");
//
//                DeviceStatus status = DeviceStatus.valueOf(statusStr);
//
//                Room room = findById(conn, roomId);
//
//                listDevices.add(new Device(deviceId, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, status, room, quantity));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return listDevices;
//    }

    public List<Device> filterAndSearch(String status, String keyword, int limitItem, int skip) {
        String sql = "SELECT * FROM devices WHERE deleted = false";
        List<Object> params = new ArrayList<>();

//        Lọc theo trạng thái
        if (status != null && !status.equals("Tất cả")) {
            sql += " AND status = ?";
            params.add(status);
        }

//        Tìm kiếm
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(device_name) LIKE ?";
            params.add("%" + keyword.toLowerCase() + "%");
        }

        sql += " ORDER BY id DESC";

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
                String deviceType = result.getString("device_type");
                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
                String supplier = result.getString("supplier");
                BigDecimal price = result.getBigDecimal("price");
                String statusStr = result.getString("status");
                String roomId = result.getString("room_id");
                int quantity = result.getInt("quantity");
                Boolean isAllow = result.getBoolean("is_borrowable");

                DeviceStatus deviceStatus = DeviceStatus.valueOf(statusStr);
                Room room = findById(conn, roomId);

                list.add(new Device(deviceId, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, deviceStatus, room, quantity, isAllow));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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

    public Boolean changeStatus(List<String> ids, String status) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "UPDATE devices set";

        boolean isDelete = status.equals("Xóa");
        if(isDelete){
            sql += " deleted = true";
        } else {
            sql += " status = ?";
        }
        sql += " WHERE id IN (" + placeholders + ")";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            int index = 1;
            if(!isDelete){
                stmt.setString(index++, status);
            }

            for (String id : ids) {
                stmt.setString(index++, id);
            }

            int result = stmt.executeUpdate();
            if(result > 0){
                return true;
            } else {
                return false;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
