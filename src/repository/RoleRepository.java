package repository;

import config.DatabaseConnection;
import model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleRepository {
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

    public Role getRoleById(int id) {
        String sql = "SELECT role_id, role_name FROM roles WHERE role_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                int roleId = rs.getInt("role_id");
                String roleName = rs.getString("role_name");
                Role role = new Role(roleId, roleName);
                return role;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean create(String name){
        String sql = "INSERT INTO roles (role_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, name);
            int rs = stmt.executeUpdate();
            if(rs > 0){
                return true;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public Boolean edit(Role role){
        String sql = "UPDATE roles SET role_name = ? WHERE role_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, role.getRoleName());
            stmt.setInt(2, role.getRoleId());
            int rs = stmt.executeUpdate();
            if(rs > 0){
                return true;
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int roleId) {
        String deletePermissionsSql = "DELETE FROM permissions WHERE role_id = ?";
        String deleteRoleSql = "DELETE FROM roles WHERE role_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (
                    PreparedStatement stmt1 = conn.prepareStatement(deletePermissionsSql);
                    PreparedStatement stmt2 = conn.prepareStatement(deleteRoleSql)
            ) {
                stmt1.setInt(1, roleId);
                stmt1.executeUpdate();

                stmt2.setInt(1, roleId);
                int result = stmt2.executeUpdate();

                conn.commit();

                return result > 0;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
