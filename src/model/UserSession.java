package model;

public class UserSession {
    private static int userId;
    private static int roleId = 3;

    public static void startSession(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static int getRoleId() {
        return roleId;
    }

    public static void clearSession() {
        userId = 0;
        roleId = 0;
    }
}

