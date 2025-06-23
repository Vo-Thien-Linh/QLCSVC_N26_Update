package repository;

import config.DatabaseConnection;
import javafx.util.Pair;
import model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BorrowRoomRepository {
    public List<String> roomTypeData(){
        List<String> datas = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE type_name != 'Kho thiết bị'";
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
        String sql = "SELECT room.*, room_types.type_name FROM room JOIN room_types ON room.room_type_id = room_types.id WHERE room.status = 'AVAILABLE' AND room.deleted = false AND room_types.type_name != 'Kho thiết bị'";
        List<Object> params = new ArrayList<>();

//        Lọc theo loại phòng
        if(typeName != null && !typeName.equals("Tất cả")){
            sql += " AND room_types.type_name = ?";
            params.add(typeName);
        }

//        Tìm kiếm
        if(keyword != null && !keyword.isBlank()){
            sql += " AND LOWER(room.room_number) LIKE ? OR LOWER(room_types.type_name) LIKE ?";
            params.add("%"+keyword.toLowerCase()+"%");
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
        String sql = "SELECT COUNT(*) FROM room JOIN room_types ON room.room_type_id = room_types.id WHERE room.status = 'AVAILABLE' AND room.deleted = false AND room_types.type_name != 'Kho thiết bị'";
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
        String query = "SELECT d.*, dt.type_name FROM devices d JOIN device_types dt ON d.device_type_id = dt.id WHERE d.room_id = ? AND available_quantity > 0 AND d.status = 'AVAILABLE' AND d.deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, roomId);

            ResultSet result = stmt.executeQuery();
            while(result.next()) {
                String id = result.getString("id");
                String thumbnail = result.getString("thumbnail");
                String deviceName = result.getString("device_name");
                String deviceType = result.getString("type_name");
                LocalDate purchaseDate = result.getDate("purchase_date").toLocalDate();
                String supplier = result.getString("supplier");
                BigDecimal price = result.getBigDecimal("price");
                String statusStr = result.getString("status");
                int quantity = result.getInt("quantity");
                int availableQuantity = result.getInt("available_quantity");

                DeviceStatus status = DeviceStatus.valueOf(statusStr);

                devices.add(new Device(id, thumbnail, deviceName, deviceType, purchaseDate, supplier, price, status, null, quantity, availableQuantity));
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
            stmt.setString(2, borrowRoom.getBorrower().getUserId());
            stmt.setDate(3, Date.valueOf(borrowRoom.getBorrowDate()));
            stmt.setInt(4, borrowRoom.getstartPeriod());
            stmt.setInt(5, borrowRoom.getEndPeriod());
            stmt.setString(6, borrowRoom.getBorrowReason());
            stmt.setString(7, "PENDING");

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer borrowRoomId = rs.getInt(1);
                    BorrowDevice borrowDevice = new BorrowDevice(0, borrowRoom.getBorrower(), borrowRoom.getBorrowDate(), borrowRoom.getstartPeriod(), borrowRoom.getEndPeriod(), BorrowStatus.PENDING, null, borrowRoomId, borrowRoom.getBorrowReason(), null, null);
                    List<BorrowDeviceDetail> devices = borrowRoom.getBorrowDeviceDetail();
                    if (devices != null && !devices.isEmpty()) {
                        return createBorrowDevice(borrowDevice, devices);
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

    public Boolean createBorrowDevice(BorrowDevice borrowDevice, List<BorrowDeviceDetail> borrowDevices) {
        String sql = "INSERT INTO borrow_device (user_id, borrow_date, start_period, end_period, borrow_status, created_at, borrow_room_id, borrow_reason) VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            stmt.setString(1, borrowDevice.getBorrower().getUserId());
            stmt.setDate(2,  Date.valueOf(borrowDevice.getBorrowDate()));
            stmt.setInt(3, borrowDevice.getStartPeriod());
            stmt.setInt(4, borrowDevice.getEndPeriod());
            stmt.setString(5, borrowDevice.getBorrowStatus().name());
            if (borrowDevice.getBorrowRoomId() != null) {
                stmt.setInt(6, borrowDevice.getBorrowRoomId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            stmt.setString(7, borrowDevice.getBorrowReason());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Integer borrowDeviceId = rs.getInt(1);
                    return createBorrowDeviceDetails(borrowDeviceId, borrowDevices);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean createBorrowDeviceDetails(int borrowDeviceRequestId, List<BorrowDeviceDetail> borrowDevices) {
        String sql = "INSERT INTO borrow_device_detail (borrow_device_id, device_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (BorrowDeviceDetail device : borrowDevices) {
                stmt.setInt(1, borrowDeviceRequestId);
                stmt.setString(2, device.getDevice().getId());
                stmt.setInt(3, device.getQuantity());
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            for (int count : results) {
                if (count == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        Map<Integer, BorrowRoom> requestMap = new LinkedHashMap<>();
        String sql = """
            SELECT 
                br.*, 
                u.fullname, 
                c.name AS className,
                dt.name AS departmentName,
                r.room_number,
                d.device_name,
                d.id AS device_id,
                d.available_quantity,
                bdd.quantity
            FROM borrow_room br 
            JOIN Users u ON br.borrower_id = u.user_id 
            LEFT JOIN classes c ON u.class_id = c.id
            LEFT JOIN departments dt ON u.department_id = dt.id
            JOIN room r ON br.room_id = r.room_id 
            LEFT JOIN borrow_device bd ON br.id = bd.borrow_room_id
            LEFT JOIN borrow_device_detail bdd ON bd.id =  bdd.borrow_device_id
            LEFT JOIN devices d ON bdd.device_id = d.id
            WHERE br.status != 'CANCELLED' AND br.borrower_id = ? AND br.created_at >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)                                              
            ORDER BY br.id DESC
        """;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, UserSession.getUserId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int borrowRoomId =  rs.getInt("id");
                if(!requestMap.containsKey(borrowRoomId)) {
                    BorrowRoom  borrowRoom = new BorrowRoom();
                    borrowRoom.setId(borrowRoomId);
                    borrowRoom.setRoomId(rs.getString("room_id"));
                    borrowRoom.setRoomNumber(rs.getString("room_number"));
                    borrowRoom.setBorrower(new User(null, rs.getString("fullname"), rs.getString("className"), rs.getString("departmentName")));
                    borrowRoom.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                    borrowRoom.setStartPeriod(rs.getInt("start_period"));
                    borrowRoom.setEndPeriod(rs.getInt("end_period"));
                    borrowRoom.setBorrowReason(rs.getString("borrow_reason"));
                    borrowRoom.setRejectReason(rs.getString("reject_reason"));
                    borrowRoom.setStatus(BorrowStatus.valueOf(rs.getString("status")));
                    requestMap.put(borrowRoomId, borrowRoom);
                }

                String deviceId = rs.getString("device_id");
                if(!rs.wasNull()) {
                    BorrowDeviceDetail borrowDeviceDetail = new BorrowDeviceDetail();
                    borrowDeviceDetail.setDevice(new Device(deviceId, rs.getString("device_name"), rs.getInt("available_quantity")));
                    borrowDeviceDetail.setQuantity(rs.getInt("quantity"));
                    requestMap.get(borrowRoomId).addDevice(borrowDeviceDetail);
                }
            }

            return new ArrayList<>(requestMap.values());
        } catch (SQLException e) {
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

    public List<BorrowRoom> getBorrowedRoomsApproved() {
        Map<Integer, BorrowRoom> requestMap = new LinkedHashMap<>();
        String sql = """
            SELECT 
                br.*, 
                u.fullname, 
                c.name AS className,
                dt.name AS departmentName,
                r.room_number,
                d.device_name,
                d.id AS device_id,
                d.available_quantity,
                bdd.quantity
            FROM borrow_room br 
            JOIN Users u ON br.borrower_id = u.user_id 
            LEFT JOIN classes c ON u.class_id = c.id
            LEFT JOIN departments dt ON u.department_id = dt.id
            JOIN room r ON br.room_id = r.room_id 
            LEFT JOIN borrow_device bd ON br.id = bd.borrow_room_id
            LEFT JOIN borrow_device_detail bdd ON bd.id =  bdd.borrow_device_id
            LEFT JOIN devices d ON bdd.device_id = d.id
            WHERE br.status = 'APPROVED' AND br.borrower_id = ?                                             
        """;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, UserSession.getUserId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int borrowRoomId =  rs.getInt("id");
                if(!requestMap.containsKey(borrowRoomId)) {
                    BorrowRoom  borrowRoom = new BorrowRoom();
                    borrowRoom.setId(borrowRoomId);
                    borrowRoom.setRoomId(rs.getString("room_id"));
                    borrowRoom.setRoomNumber(rs.getString("room_number"));
                    borrowRoom.setBorrower(new User(null, rs.getString("fullname"), rs.getString("className"), rs.getString("departmentName")));
                    borrowRoom.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                    borrowRoom.setStartPeriod(rs.getInt("start_period"));
                    borrowRoom.setEndPeriod(rs.getInt("end_period"));
                    borrowRoom.setBorrowReason(rs.getString("borrow_reason"));
                    borrowRoom.setRejectReason(rs.getString("reject_reason"));
                    borrowRoom.setStatus(BorrowStatus.valueOf(rs.getString("status")));
                    requestMap.put(borrowRoomId, borrowRoom);
                }

                String deviceId = rs.getString("device_id");
                if(!rs.wasNull()) {
                    BorrowDeviceDetail borrowDeviceDetail = new BorrowDeviceDetail();
                    borrowDeviceDetail.setDevice(new Device(deviceId, rs.getString("device_name"), rs.getInt("available_quantity")));
                    borrowDeviceDetail.setQuantity(rs.getInt("quantity"));
                    requestMap.get(borrowRoomId).addDevice(borrowDeviceDetail);
                }
            }

            return new ArrayList<>(requestMap.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
