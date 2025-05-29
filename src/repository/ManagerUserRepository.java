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

    //Thêm mới người dùng
    public Boolean addUserAndReturnID(User user) {
        int roleId = getRoleIdByRoleName(user.getRole().getRoleName());
        String query = "INSERT INTO users (fullname, username, yearold, email, phoneNumber, password, status, role_id, thumbnail) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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


            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


//    public List<User> getAllUsers() {
//        List<User> users = new ArrayList<>();
//        String query = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.deleted = false";
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                String userId = rs.getString("user_id");
//                String fullname = rs.getString("fullname");
//                String username = rs.getString("username");
//                String thumbnail = rs.getString("thumbnail");
//                String yearold = rs.getString("yearold");
//                String email = rs.getString("email");
//                String phoneNumber = rs.getString("phoneNumber");
//                String password = rs.getString("password");
//                String statusString = rs.getString("status");
//                String roleString = rs.getString("role_name");
//
//                Status status;
//                status = Status.valueOf(statusString);
//
//                RoleName roleName = RoleName.valueOf(roleString);
//                Role role = new Role();
//                role.setRoleName(roleName);
//
//                users.add(new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, status, role));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return users;
//    }

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

    //	Chỉnh sửa người dùng
    public Boolean edit(User manager) {
        String query = "UPDATE users SET fullname = ?, username = ?, yearold = ?, email = ?, phoneNumber = ?, status = ?, role_id = ?, thumbnail = ? WHERE user_id = ? AND deleted = false";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setString(1, manager.getFullname());
            stmt.setString(2, manager.getUsername());
            stmt.setDate(3, Date.valueOf(manager.getYearold()));
            stmt.setString(4, manager.getEmail());
            stmt.setString(5, manager.getPhoneNumber());
            stmt.setString(6, manager.getStatus().name());
            stmt.setInt(7, manager.getRole().getRoleId());
            stmt.setString(8, manager.getThumbnail());
            stmt.setString(9, manager.getUserId());

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

    //	xóa người dùng
    public Boolean delete(String userId) {
        String query = "UPDATE users SET deleted = true WHERE user_id = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);

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


    public Boolean changeStatus(List<String> ids, String status) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "UPDATE users set";

        boolean isDelete = status.equals("Xóa");
        if(isDelete){
            sql += " deleted = true";
        } else {
            sql += " status = ?";
        }
        sql += " WHERE user_id IN (" + placeholders + ")";

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

    public List<User> filterAndSearch(String status, String keyword, int limitItem, int skip) {
        String sql = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.deleted = false";
        List<Object> params = new ArrayList<>();

//        Lọc theo trạng thái
        if (status != null && !status.equals("Tất cả")) {
            sql += " AND u.status = ?";
            params.add(status);
        }

//        Tìm kiếm
        if (keyword != null && !keyword.isBlank()) {
            sql += " AND LOWER(u.fullname) LIKE ?";
            params.add("%" + keyword.toLowerCase() + "%");
        }

        sql += " ORDER BY u.user_id DESC";

//        Phân trang
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

                Status statusUser;
                statusUser = Status.valueOf(statusString);

                Role role = new Role();
                role.setRoleName(roleString);

                list.add(new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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
        String sql = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.deleted = false AND user_id = ?";

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
                String roleString = rs.getString("role_name");

                Status statusUser;
                statusUser = Status.valueOf(statusString);

                Role role = new Role();
                role.setRoleName(roleString);

                return new User(userId, fullname, username, thumbnail, yearold, email, phoneNumber, password, statusUser, role);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
