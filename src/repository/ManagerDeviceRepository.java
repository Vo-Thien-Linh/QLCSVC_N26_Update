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
        String query = "SELECT r.room_id, r.room_number, rt.type_name  FROM room r JOIN room_types rt ON r.room_type_id = rt.id WHERE r.status = 'AVAILABLE'";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet result = stmt.executeQuery();
            while(result.next()) {
                String roomId = result.getString("room_id");
                String roomNumber = result.getString("room_number");
                String typeName =  result.getString("type_name");
                rooms.add(new Room(roomId, roomNumber,  typeName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public static ArrayList<String> getAllDeviceTypes(){
        ArrayList<String> deviceTypes = new ArrayList<>();
        String query = "SELECT type_name FROM device_types";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet result = stmt.executeQuery();
            while(result.next()) {
                String deviceType = result.getString("type_name");
                deviceTypes.add(deviceType);
            }
            return deviceTypes;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getDeviceTypeId(String deviceType){
        String sql = "SELECT id FROM device_types WHERE type_name = ?";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, deviceType);
            ResultSet result = stmt.executeQuery();
            if(result.next()) {
                return result.getInt("id");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Boolean create(Device device) {
        int deviceTypeId = getDeviceTypeId(device.getDeviceType());
        String query = "INSERT INTO devices (device_name, device_type_id, purchase_date, supplier, price, status, room_id, quantity, created_at, updated_at, thumbnail) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setString(1, device.getDeviceName());
            stmt.setInt(2, deviceTypeId);
            stmt.setDate(3, Date.valueOf(device.getPurchaseDate()));
            stmt.setString(4, device.getSupplier());
            stmt.setBigDecimal(5, device.getPrice());
            stmt.setString(6, device.getStatus().name());
            stmt.setString(7, device.getRoom().getId());
            stmt.setInt(8, device.getQuantity());
            stmt.setDate(9, Date.valueOf(device.getCreatedAt()));
            stmt.setDate(10, Date.valueOf(device.getUpdatedAt()));
            stmt.setString(11, device.getThumbnail());

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
        String query = "SELECT r.room_number, rt.type_name  FROM room r JOIN room_types rt ON r.room_type_id = rt.id WHERE room_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, roomId);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return new Room(roomId, rs.getString("room_number"), rs.getString("type_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Device DataDevice(String deviceId) {
        String query = "SELECT d.*, dt.type_name FROM devices d JOIN device_types dt ON d.device_type_id = dt.id WHERE d.id = ? AND d.deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, deviceId);

            ResultSet result = stmt.executeQuery();
            if(result.next()) {
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

                DeviceStatus status = DeviceStatus.valueOf(statusStr);

                Room room = findById(conn, roomId);

                return new Device(null, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, status, room, quantity, availableQuantity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean edit(Device device) {
        int deviceTypeId = getDeviceTypeId(device.getDeviceType());
        String query = "UPDATE devices SET device_name = ?, device_type_id = ?, purchase_date = ?, supplier = ?, price = ?, status = ?, room_id = ?, quantity = ?, updated_at = ?, thumbnail = ? WHERE id = ? AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, device.getDeviceName());
            stmt.setInt(2, deviceTypeId);
            stmt.setDate(3, Date.valueOf(device.getPurchaseDate()));
            stmt.setString(4, device.getSupplier());
            stmt.setBigDecimal(5, device.getPrice());
            stmt.setString(6, device.getStatus().name());
            stmt.setString(7, device.getRoom().getId());
            stmt.setInt(8, device.getQuantity());
            stmt.setDate(9, Date.valueOf(device.getUpdatedAt()));
            stmt.setString(10, device.getThumbnail());
            stmt.setString(11, device.getId());

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

    public List<Device> filterAndSearch(String status, String keyword, int limitItem, int skip) {
        String sql = "SELECT d.*, dt.type_name FROM devices d JOIN device_types dt ON d.device_type_id = dt.id WHERE d.deleted = false";
        List<Object> params = new ArrayList<>();

//        Lọc theo trạng thái
        if (status != null && !status.equals("Tất cả")) {
            sql += " AND d.status = ?";
            params.add(status);
        }

//        Tìm kiếm
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(d.device_name) LIKE ?";
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
