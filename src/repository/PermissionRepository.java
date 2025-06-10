package repository;

import config.DatabaseConnection;
import model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermissionRepository {
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

    public int getRoleIdByName(String roleName) {
        String sql = "SELECT role_id FROM roles WHERE role_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("role_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getFunctionIdByName(String functionName) {
        String sql = "SELECT id FROM functions WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, functionName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getIdByFeatureName(String featureName) {
        String sql = "SELECT id FROM permission_types WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, featureName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Boolean updatePermission(int roleId, int functionId, int permissionTypeId, boolean allowed) {
        String sql = "REPLACE INTO permissions (role_id, function_id, permission_type_id, allowed) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            stmt.setInt(2, functionId);
            stmt.setInt(3, permissionTypeId);
            stmt.setBoolean(4, allowed);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getAllFunctions() {
        List<String> functions = new ArrayList<>();
        String sql = "SELECT name FROM functions ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                functions.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return functions;
    }

    public List<String> getPermissionTypesByFunction(String functionName) {
        List<String> types = new ArrayList<>();
        String sql = """
            SELECT DISTINCT pt.name
            FROM permissions p
            JOIN functions f ON p.function_id = f.id
            JOIN permission_types pt ON p.permission_type_id = pt.id
            WHERE f.name = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, functionName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    types.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return types;
    }

    public boolean testConnection() {
        String sql = "SELECT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Database connection successful!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Database connection failed!");
        return false;
    }

    public boolean isAllowed(int roleId, String functionName, String permissionTypeName) {
        System.out.println("Checking permission: roleId=" + roleId + ", functionName='" + functionName + "', permissionTypeName='" + permissionTypeName + "'");
        System.out.println("Testing connection: " + testConnection());

        String sql = """
            SELECT p.allowed
            FROM permissions p
            JOIN functions f ON p.function_id = f.id
            JOIN permission_types pt ON p.permission_type_id = pt.id
            WHERE p.role_id = ? AND f.name = ? AND pt.name = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            stmt.setString(2, functionName);
            stmt.setString(3, permissionTypeName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean allowed = rs.getBoolean("allowed");
                    System.out.println("Permission found: allowed=" + allowed);
                    return allowed;
                } else {
                    System.out.println("No permission record found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Returning default: false");
        return false;
    }
}