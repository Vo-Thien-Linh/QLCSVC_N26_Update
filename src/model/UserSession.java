package model;

public class UserSession {
    private static String userId = "MTL0001";
    private static int roleId = 5;

    public static void startSession(String id) {
        userId = id;
    }

    public static String getUserId() {
        return userId;
    }

    public static int getRoleId() {
        return roleId;
    }

    public static void clearSession() {
        userId = "";
        roleId = 0;
    }
}

