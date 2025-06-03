package repository;

import config.DatabaseConnection;
import javafx.util.Pair;
import model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BorrowRoomRepository {
    public List<String> roomTypeData(){
        List<String> datas = new ArrayList<>();
        String sql = "SELECT * FROM room_types";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                datas.add(rs.getString("type_name"));
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return datas;
    }

    public List<Room> roomData(){
        List<Room> datas = new ArrayList<>();
        String sql = "SELECT room.*, room_types.type_name FROM room JOIN room_types ON room.room_type_id = room_types.id WHERE room.status = 'AVAILABLE' AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String roomNumber = rs.getString("room_number");
                String roomType = rs.getString("type_name");
                int capacity = rs.getInt("seating_capacity");
                String location = rs.getString("location");
                String statusStr = rs.getString("status");

                RoomStatus status = RoomStatus.valueOf(statusStr);
                datas.add(new Room(null, status, roomNumber, capacity, roomType, location));
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return datas;
    }

    public List<Room> filterAndSearch(String typeName, String keyword, int limit, int skip){
        List<Room> datas = new ArrayList<>();
        String sql = "SELECT room.*, room_types.type_name FROM room JOIN room_types ON room.room_type_id = room_types.id WHERE room.status = 'AVAILABLE' AND deleted = false";
        List<Object> params = new ArrayList<>();

//        Lọc theo loại phòng
        if(typeName != null && !typeName.equals("Tất cả")){
            sql += " AND room_types.type_name = ?";
            params.add(typeName);
        }

//        Tìm kiếm
        if(keyword != null && !keyword.isBlank()){
            sql += " AND LOWER(room.room_number) LIKE ?";
            params.add("%"+keyword.toLowerCase()+"%");
        }

        //        Phân trang
        sql += " LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(skip);

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){

            for(int i = 0; i < params.size(); i++){
                stmt.setObject(i+1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                String id = rs.getString("room_id");
                String roomNumber = rs.getString("room_number");
                String roomType = rs.getString("type_name");
                int capacity = rs.getInt("seating_capacity");
                String location = rs.getString("location");
                String statusStr = rs.getString("status");
                RoomStatus status = RoomStatus.valueOf(statusStr);
                datas.add(new Room(id, status, roomNumber, capacity, roomType, location));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return datas;
    }

    public int countRoom(String typeName, String keyword){
        String sql = "SELECT COUNT(*) FROM room JOIN room_types ON room.room_type_id = room_types.id WHERE room.status = 'AVAILABLE' AND deleted = false";
        List<Object> params = new ArrayList<>();

//        Lọc theo loại phòng
        if(typeName != null && !typeName.equals("Tất cả")){
            sql += " AND room_types.type_name = ?";
            params.add(typeName);
        }

//        Tìm kiếm
        if(keyword != null && !keyword.isBlank()){
            sql += " AND LOWER(room.room_number) LIKE ?";
            params.add("%"+keyword.toLowerCase()+"%");
        }

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            for(int i = 0; i < params.size(); i++){
                stmt.setObject(i+1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public List<Device> dataDeviceByRoom(String roomId) {
        List<Device> devices = new ArrayList<>();
        String query = "SELECT * FROM devices WHERE room_id = ? AND status = 'AVAILABLE' AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, roomId);

            ResultSet result = stmt.executeQuery();
            while(result.next()) {
                String id = result.getString("id");
                String thumbnail = result.getString("thumbnail");
                String deviceName = result.getString("device_name");
                String deviceType = result.getString("device_type");
                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
                String supplier = result.getString("supplier");
                BigDecimal price = result.getBigDecimal("price");
                String statusStr = result.getString("status");
                int quantity = result.getInt("quantity");
                Boolean isAllow = result.getBoolean("is_borrowable");

                DeviceStatus status = DeviceStatus.valueOf(statusStr);

                devices.add(new Device(id, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, status, null, quantity, isAllow));
            }

            return devices;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isRoomScheduleConflict(String roomId, LocalDate borrowDate, int startPeriod, int endPeriod) {
        String sql = "SELECT 1 FROM borrow_room WHERE room_id = ? AND borrow_date = ? AND status IN ('PENDING', 'APPROVED') AND (? <= end_period AND ? >= start_period) LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomId);
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

    public Boolean createBorrowRoom(BorrowRoom borrowRoom) {
        String sql = "INSERT INTO borrow_room(room_id, borrower_id, borrow_date, start_period, end_period, borrow_reason, status) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){

            stmt.setString(1, borrowRoom.getRoomId());
            stmt.setString(2, borrowRoom.getBorrowerId());
            stmt.setDate(3, Date.valueOf(borrowRoom.getBorrowDate()));
            stmt.setInt(4, borrowRoom.getstartPeriod());
            stmt.setInt(5, borrowRoom.getEndPeriod());
            stmt.setString(6, borrowRoom.getBorrowReason());
            stmt.setString(7, "PENDING");

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int borrowRoomId = rs.getInt(1);
                    List<BorrowDevice> devices = borrowRoom.getBorrowDevices();
                    if (devices != null && !devices.isEmpty()) {
                        return createBorrowDevice(borrowRoomId, devices);
                    } else {
                        return true;
                    }
                }

            }



        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public Boolean createBorrowDevice(int borrowRoonId, List<BorrowDevice> borrowDevices) {
        String sql = "INSERT INTO borrow_device (borrow_room_id, device_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (BorrowDevice item : borrowDevices) {
                stmt.setInt(1, borrowRoonId);
                stmt.setString(2, item.getDeviceId());
                stmt.setInt(3, item.getQuantity());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            for (int count : results) {
                if (count == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }

            return true;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public List<Pair<Integer, Integer>> getBookedPeriodSlots(String roomId, LocalDate borrowDate) {
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        String sql = "SELECT start_period, end_period FROM borrow_room WHERE room_id = ? AND borrow_date = ? AND status IN ('APPROVED', 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomId);
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

    public void updateRoomStatus(String roomId, String status) {
        String sql = "UPDATE room SET status = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, roomId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<BorrowRoom> getBorrowedRooms() {
        List<BorrowRoom> list = new ArrayList<>();
        String sql = "SELECT br.*, br.status AS borrowStatus, r.room_number FROM borrow_room br JOIN room r ON br.room_id = r.room_id WHERE borrower_id = ? AND br.status != 'CANCELLED'";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, UserSession.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String roomId = rs.getString("room_id");
                String roomNumber = rs.getString("room_number");
                LocalDate borrowDate = rs.getDate("borrow_date").toLocalDate();
                Integer startPeriod = rs.getInt("start_period");
                Integer endPeriod = rs.getInt("end_period");
                String statusStr = rs.getString("borrowStatus");
                BorrowRoomStatus status = BorrowRoomStatus.valueOf(statusStr.toUpperCase());

                list.add(new BorrowRoom(id, roomId, roomNumber, UserSession.getUserId(), borrowDate, startPeriod, endPeriod, null, null, status, null));
            }
            return list;

        } catch (SQLException e){
            e.printStackTrace();
        }

        return null;
    }

    public Boolean cancle(int borrowId) {
        String sql = "UPDATE borrow_room SET status = 'CANCELLED' WHERE id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, borrowId);
            int rs = stmt.executeUpdate();
            if(rs > 0) {
                return true;
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
