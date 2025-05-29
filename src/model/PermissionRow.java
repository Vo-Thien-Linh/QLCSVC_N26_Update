package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRow {
    private String feature;
    private Map<String, Boolean> permissions;
    private boolean isHeader;

    public PermissionRow(String feature, List<Role> roles, boolean isHeader) {
        this.feature = feature;
        this.isHeader = isHeader;
        this.permissions = new HashMap<>();
        for (Role role : roles) {
            permissions.put(role.toString(), false);
        }
    }

    public String getFeature() {
        return feature;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public Boolean getPermission(String roleName) {
        return permissions.get(roleName);
    }

    public void setPermission(String roleName, Boolean value) {
        permissions.put(roleName, value);
    }

    public boolean isHeader() {
        return isHeader;
    }
}

