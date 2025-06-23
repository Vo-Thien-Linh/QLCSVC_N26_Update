package repository;

import config.DatabaseConnection;
import javafx.util.Pair;
import model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowDeviceRepository {

    public List<Device> search(String keyword, int limitItem, int skip) {
        String sql = """
            SELECT d.*, dt.type_name, r.room_number, rt.type_name, r.location 
            FROM devices d 
            JOIN device_types dt ON d.device_type_id = dt.id 
            JOIN room r ON d.room_id = r.room_id 
            JOIN room_types rt ON r.room_type_id = rt.id 
            WHERE d.deleted = false AND rt.type_name = 'Kho thiết bị' AND d.available_quantity > 0 AND d.status = 'AVAILABLE'
        """;
        List<Object> params = new ArrayList<>();

//        Tìm kiếm
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(d.device_name) LIKE ? OR LOWER(dt.type_name) LIKE ? OR LOWER(d.id) LIKE ?";
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
                String roomNumber = result.getString("room_number");
                String roomType  = result.getString("type_name");
                String  location = result.getString("location");
                int quantity = result.getInt("quantity");
                int availableQuantity = result.getInt("available_quantity");
                System.out.println(roomId);

                DeviceStatus deviceStatus = DeviceStatus.valueOf(statusStr);
                Room room = new Room(roomId, null, roomNumber, 0, roomType, location);

                list.add(new Device(deviceId, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, deviceStatus, room, quantity, availableQuantity));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int countDevices(String keyword) {
        String sql = """
            SELECT COUNT(*)
            FROM devices d 
            JOIN device_types dt ON d.device_type_id = dt.id 
            JOIN room r ON d.room_id = r.room_id 
            JOIN room_types rt ON r.room_type_id = rt.id 
            WHERE d.deleted = false AND rt.type_name = 'Kho thiết bị' AND d.available_quantity > 0 AND d.status = 'AVAILABLE'
        """;
        List<Object> params = new ArrayList<>();

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

    public List<Pair<Integer, Integer>> getBookedPeriodSlots(String deviceId, LocalDate borrowDate) {
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        String sql = "SELECT bd.start_period, bd.end_period FROM borrow_device bd JOIN borrow_device_detail bdd ON bd.id = bdd.borrow_device_id  WHERE bdd.device_id = ? AND bd.borrow_date = ? AND bd.borrow_status IN ('APPROVED', 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deviceId);
            stmt.setDate(2, Date.valueOf(borrowDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Integer start = rs.getInt("start_period");
                Integer end = rs.getInt("end_period");
                list.add(new Pair<>(start, end));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean isDeviceScheduleConflict(String deviceId, LocalDate borrowDate, int startPeriod, int endPeriod) {
        String sql = "SELECT 1 FROM borrow_device bd JOIN borrow_device_detail bdd ON bd.id = bdd.borrow_device_id WHERE bdd.device_id = ? AND bd.borrow_date = ? AND bd.borrow_status IN ('PENDING', 'APPROVED') AND (? <= bd.end_period AND ? >= bd.start_period) LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deviceId);
            stmt.setDate(2, Date.valueOf(borrowDate));
            stmt.setInt(3, startPeriod);
            stmt.setInt(4, endPeriod);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public Boolean createBorrowDevice(BorrowDevice borrowDevice) {
        String sql = "INSERT INTO borrow_device (user_id, borrow_date, start_period, end_period, borrow_status, created_at, borrow_room_id, borrow_reason) VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            stmt.setString(1, borrowDevice.getBorrower().getUserId());
            stmt.setDate(2,  Date.valueOf(borrowDevice.getBorrowDate()));
            stmt.setInt(3, borrowDevice.getStartPeriod());
            stmt.setInt(4, borrowDevice.getEndPeriod());
            stmt.setString(5, "PENDING");
            stmt.setNull(6, java.sql.Types.INTEGER);

            stmt.setString(7, borrowDevice.getBorrowReason());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer borrowDeviceId = rs.getInt(1);
                    return createBorrowDeviceDetails(borrowDeviceId, borrowDevice.getBorrowDeviceDetail());
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean createBorrowDeviceDetails(int borrowDeviceRequestId, BorrowDeviceDetail borrowDeviceDetail) {
        String sql = "INSERT INTO borrow_device_detail (borrow_device_id, device_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, borrowDeviceRequestId);
            stmt.setString(2, borrowDeviceDetail.getDevice().getId());
            stmt.setInt(3, borrowDeviceDetail.getQuantity());
            stmt.addBatch();

            int result = stmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BorrowDevice> getAllBorrowDevices(){
        String sql = "SELECT bd.*, d.device_name, d.id AS device_id, u.user_id, u.fullname, c.name AS className, dt.name AS departmentName, bdd.quantity, bdd.id AS borrowDeviceDetailId FROM borrow_device bd JOIN borrow_device_detail bdd ON bd.id = bdd.borrow_device_id JOIN devices d ON d.id = bdd.device_id JOIN Users u ON u.user_id = bd.user_id LEFT JOIN classes c ON u.class_id = c.id LEFT JOIN departments dt ON u.department_id = dt.id WHERE bd.borrow_room_id IS NULL AND bd.borrow_status != 'CANCELLED' AND bd.user_id = ? AND bd.created_at >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)";
        List<BorrowDevice> borrowDevices = new ArrayList<>();
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, UserSession.getUserId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int borrowDeviceId =  rs.getInt("id");
                String userId = rs.getString("user_id");
                String fullname = rs.getString("fullname");
                String className = rs.getString("className");
                String departmentName = rs.getString("departmentName");
                LocalDate  borrowDate = LocalDate.parse(rs.getString("borrow_date"));
                int startPeriod = rs.getInt("start_period");
                int endPeriod = rs.getInt("end_period");
                BorrowStatus status = BorrowStatus.valueOf(rs.getString("borrow_status"));
                LocalDateTime createAt = rs.getTimestamp("created_at").toLocalDateTime();
                String borrowReason =  rs.getString("borrow_reason");
                String rejectReason = rs.getString("reject_reason");
                String deviceId =  rs.getString("device_id");
                String deviceName = rs.getString("device_name");
                int quantity = rs.getInt("quantity");
                int borrowDeviceDetailId  = rs.getInt("borrowDeviceDetailId");

                User borrower =  new User(userId, fullname, className, departmentName);
                BorrowDeviceDetail borrowDeviceDetail = new BorrowDeviceDetail(borrowDeviceDetailId, 0, new Device(deviceId, deviceName, 0), quantity);
                borrowDevices.add( new BorrowDevice(borrowDeviceId, borrower, borrowDate, startPeriod, endPeriod, status, createAt, null, borrowReason, rejectReason, borrowDeviceDetail));
            }

            return borrowDevices;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Boolean cancle(int id){
        String sql = "UPDATE borrow_device SET borrow_status = 'CANCELLED' WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, id);

            int rs = stmt.executeUpdate();
            if (rs > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Boolean updateDeviceReturn(int id, int quantity, String note){
        String sql = "INSERT INTO return_device_detail(borrow_device_detail_id, return_quantity, condition_note, return_time) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.setInt(2, quantity);
            stmt.setString(3, note);

            int rs = stmt.executeUpdate();
            return rs > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  false;
    }

    public void updateBorrowDeviceStatus(int borrowDeviceId) {
        String sql = "UPDATE borrow_device SET borrow_status = 'RETURNED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, borrowDeviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateDeviceAvailableQuantity(String deviceId, int brokenQuantity) {
        System.out.println(deviceId);
        String sql = "UPDATE devices SET available_quantity = available_quantity - ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, brokenQuantity);
            stmt.setString(2, deviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
