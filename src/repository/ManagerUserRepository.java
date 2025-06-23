package repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import config.DatabaseConnection;
import model.*;

public class ManagerUserRepository {

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String query = "SELECT role_id, role_name FROM roles";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("role_id");
                String roleName = rs.getString("role_name");
                Role role = new Role(id, roleName);
                roles.add(role);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roles;
    }

    public List<String> getAllDepartment() {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT name FROM departments";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public List<String> getAllClass() {
        List<String> classes = new ArrayList<>();
        String sql = "SELECT name FROM classes";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                classes.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public int getRoleIdByRoleName(String roleName) {
        int roleId = -1;

        String sql = "SELECT role_id FROM roles WHERE role_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                roleId = rs.getInt("role_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roleId;
    }

    public int getClassId(String name){
        String sql = "SELECT id FROM classes WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int getDepartmentId(String name){
        String sql = "SELECT id FROM departments WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Thêm mới người dùng
    public Boolean addUserAndReturnID(User user) {
        int roleId = getRoleIdByRoleName(user.getRole().getRoleName());
        Integer classId = null;
        Integer departmentId = null;

        if ("Sinh Viên".equals(user.getRole().getRoleName())) {
            classId = getClassId(user.getClasses());
        } else if("Giáo viên".equals(user.getRole().getRoleName()) || "Bảo trì".equals(user.getRole().getRoleName())) {
            departmentId = getDepartmentId(user.getDepartment());
        }

        String query = "INSERT INTO users (fullname, username, yearold, email, phoneNumber, password, status, role_id, thumbnail, class_id, department_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getFullname());
            stmt.setString(2, user.getUsername());
            stmt.setDate(3, Date.valueOf(user.getYearold()));
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPhoneNumber());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getStatus().name());
            stmt.setInt(8, roleId);
            stmt.setString(9, user.getThumbnail());

            if (classId != null) {
                stmt.setInt(10, classId);
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }

            if (departmentId != null) {
                stmt.setInt(11, departmentId);
            } else {
                stmt.setNull(11, java.sql.Types.INTEGER);
            }

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isUsernameExists(String userId, String username) {
        String query = "SELECT user_id FROM users WHERE username = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEmailExists(String userId, String email) {
        String query = "SELECT * FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isNumberPhoneExists(String userId, String phoneNumber) {
        String query = "SELECT * FROM users WHERE phoneNumber = ? AND user_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phoneNumber);
            stmt.setString(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Chỉnh sửa người dùng
    public Boolean edit(User manager) {
        Integer classId = null;
        Integer departmentId = null;

        if ("Sinh Viên".equals(manager.getRole().getRoleName())) {
            classId = getClassId(manager.getClasses());
        } else if("Giáo viên".equals(manager.getRole().getRoleName()) || "Bảo trì".equals(manager.getRole().getRoleName())) {
            departmentId = getDepartmentId(manager.getDepartment());
        }

        String query = "UPDATE users SET fullname = ?, username = ?, yearold = ?, email = ?, phoneNumber = ?, status = ?, role_id = ?, thumbnail = ?, class_id = ?, department_id = ? WHERE user_id = ? AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){

            System.out.println("Cập nhật người dùng: user_id=" + manager.getUserId() + ", password=" + manager.getPassword());
            stmt.setString(1, manager.getFullname());
            stmt.setString(2, manager.getUsername());
            stmt.setDate(3, Date.valueOf(manager.getYearold()));
            stmt.setString(4, manager.getEmail());
            stmt.setString(5, manager.getPhoneNumber());
            stmt.setString(6, manager.getStatus().name());
            stmt.setInt(7, manager.getRole().getRoleId());
            stmt.setString(8, manager.getThumbnail());

            if (classId != null) {
                stmt.setInt(9, classId);
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }

            if (departmentId != null) {
                stmt.setInt(10, departmentId);
            } else {
                stmt.setNull(10, java.sql.Types.INTEGER);
            }

            stmt.setString(11, manager.getUserId());

            int result = stmt.executeUpdate();
            System.out.println("Kết quả cập nhật: " + result + " hàng bị ảnh hưởng");

            if (result > 0) {
                return true;
            } else {
                System.out.println("Không có hàng nào được cập nhật. Kiểm tra điều kiện WHERE.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Xóa người dùng
    public Boolean delete(String userId) {
        String query = "UPDATE users SET deleted = true WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);

            int result = stmt.executeUpdate();
            if (result > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean changeStatus(List<String> ids, String status) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "UPDATE users set";

        boolean isDelete = status.equals("Xóa");
        if (isDelete) {
            sql += " deleted = true";
        } else {
            sql += " status = ?";
        }
        sql += " WHERE user_id IN (" + placeholders + ")";

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
            if (result > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> filterAndSearch(String status, String keyword, int limitItem, int skip) {
        String sql = "SELECT u.*, r.role_name, c.name AS className, d.name AS departmentName FROM users u JOIN roles r ON u.role_id = r.role_id LEFT JOIN classes c ON u.class_id = c.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.deleted = false";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.equals("Tất cả")) {
            sql += " AND u.status = ?";
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(u.user_id) LIKE ? OR LOWER(u.fullname) LIKE ?";
            params.add("%" + keyword.toLowerCase() + "%");
            params.add("%" + keyword.toLowerCase() + "%");
        }

        sql += " ORDER BY u.user_id DESC";
        sql += " LIMIT ? OFFSET ?";
        params.add(limitItem);
        params.add(skip);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String fullname = rs.getString("fullname");
                String username = rs.getString("username");
                String thumbnail = rs.getString("thumbnail");
                LocalDate yearold = rs.getDate("yearold").toLocalDate();
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");
                String password = rs.getString("password");
                String statusString = rs.getString("status");
                String roleString = rs.getString("role_name");
                String className = rs.getString("className");
                String departmentName = rs.getString("departmentName");

                Status statusUser = Status.valueOf(statusString);
                Role role = new Role();
                role.setRoleName(roleString);

                list.add(new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role, className, departmentName));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int countDevices(String status, String keyword) {
        String sql = "SELECT COUNT(*) FROM users WHERE deleted = false";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.equals("Tất cả")) {
            sql += " AND status = ?";
            params.add(status);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(fullname) LIKE ?";
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

    public User getUser(String userId) {
        String sql = "SELECT u.*, r.role_name, c.name AS className, d.name AS departmentName FROM users u JOIN roles r ON u.role_id = r.role_id LEFT JOIN classes c ON u.class_id = c.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.deleted = false AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String fullname = rs.getString("fullname");
                String username = rs.getString("username");
                String thumbnail = rs.getString("thumbnail");
                LocalDate yearold = rs.getDate("yearold").toLocalDate();
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");
                String password = rs.getString("password");
                String statusString = rs.getString("status");
                int roleId = rs.getInt("role_id");
                String roleString = rs.getString("role_name");
                String className = rs.getString("className");
                String departmentName = rs.getString("departmentName");

                Status statusUser = Status.valueOf(statusString);
                Role role = new Role();
                role.setRoleId(roleId);
                role.setRoleName(roleString);

                return new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role, className, departmentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT u.*, r.role_id, r.role_name, c.name AS className, d.name AS departmentName FROM users u JOIN roles r ON u.role_id = r.role_id LEFT JOIN classes c ON u.class_id = c.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.deleted = false AND u.username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                String fullname = rs.getString("fullname");
                String thumbnail = rs.getString("thumbnail");
                LocalDate yearold = rs.getDate("yearold").toLocalDate();
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");
                String password = rs.getString("password");
                String statusString = rs.getString("status");
                int roleId = rs.getInt("role_id");
                String roleString = rs.getString("role_name");
                String className = rs.getString("className");
                String departmentName = rs.getString("departmentName");

                Status statusUser = Status.valueOf(statusString);
                Role role = new Role(roleId, roleString);

                return new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role, className, departmentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT u.*, r.role_id, r.role_name, c.name AS className, d.name AS departmentName FROM users u JOIN roles r ON u.role_id = r.role_id LEFT JOIN classes c ON u.class_id = c.id LEFT JOIN departments d ON u.department_id = d.id WHERE u.deleted = false AND u.email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");
                String fullname = rs.getString("fullname");
                String username = rs.getString("username");
                String thumbnail = rs.getString("thumbnail");
                LocalDate yearold = rs.getDate("yearold").toLocalDate();
                String phoneNumber = rs.getString("phoneNumber");
                String password = rs.getString("password");
                String statusString = rs.getString("status");
                int roleId = rs.getInt("role_id");
                String roleString = rs.getString("role_name");
                String className = rs.getString("className");
                String departmentName = rs.getString("departmentName");

                Status statusUser = Status.valueOf(statusString);
                Role role = new Role(roleId, roleString);

                return new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role, className, departmentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getUsersByRole(int roleId) throws SQLException {
        String sql = "SELECT * FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.role_id = ? AND u.deleted = FALSE";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getString("user_id"),
                        rs.getString("fullname"),
                        rs.getString("username"),
                        rs.getString("thumbnail"),
                        rs.getDate("yearold").toLocalDate(),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getString("password"),
                        Status.valueOf(rs.getString("status")),
                        new Role(rs.getInt("role_id"), rs.getString("role_name")),
                        null, // classes
                        null  // department
                );
                users.add(user);
            }
        }
        return users;
    }

    public User getUserById(String userId) throws SQLException {
        String sql = "SELECT * FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.user_id = ? AND u.deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userIdVal = rs.getString("user_id");
                String fullname = rs.getString("fullname");
                String username = rs.getString("username");
                String thumbnail = rs.getString("thumbnail");
                LocalDate yearold = rs.getDate("yearold").toLocalDate();
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phoneNumber");
                String password = rs.getString("password");
                String statusString = rs.getString("status");
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("role_name");

                Status status = Status.valueOf(statusString);
                Role role = new Role(roleId, roleName);

                return new User(userIdVal, fullname, username, thumbnail, yearold, email, phoneNumber, password, status, role, null, null);
            }
        }
        return null;
    }

    public void updateRoomStatus(Room room) throws SQLException {
        String sql = "UPDATE room SET status = ?, maintained_by = ? WHERE room_id = ?";
        System.out.println("Thực hiện cập nhật trạng thái phòng: room_id=" + room.getId() + ", status=" + room.getStatus() + ", maintained_by=" + room.getMaintainedBy());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getStatus().name()); // Đảm bảo gán đúng giá trị enum
            stmt.setString(2, room.getMaintainedBy());
            stmt.setString(3, room.getId());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Số hàng bị ảnh hưởng: " + rowsAffected);
            if (rowsAffected == 0) {
                throw new SQLException("Không có hàng nào được cập nhật, kiểm tra room_id hoặc quyền truy cập.");
            }
        }
    }

    public void updateDeviceStatus(Device device) throws SQLException {
        String sql = "UPDATE devices SET status = ?, maintained_by = ? WHERE id = ?";
        System.out.println("Thực hiện cập nhật trạng thái thiết bị: id=" + device.getId() + ", status=" + device.getStatus() + ", maintained_by=" + device.getMaintainedBy());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, device.getStatus().name());
            stmt.setString(2, device.getMaintainedBy());
            stmt.setString(3, device.getId());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Số hàng bị ảnh hưởng: " + rowsAffected);
            if (rowsAffected == 0) {
                throw new SQLException("Không có hàng nào được cập nhật, kiểm tra device_id hoặc quyền truy cập.");
            }
        }
    }

    public Boolean updatePassword(String id, String password){
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, password);
            stmt.setString(2, id);
            int rs = stmt.executeUpdate();
            return rs > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}