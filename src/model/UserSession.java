package model;

public class UserSession {
    private static String userId = null;
    private static int roleId = 0;
    private static String currentUserId;


    public static void startSession(String id, int role) {
        userId = id;
        roleId = role;
    }

    public static String getUserId() {
        return userId;
    }

    public static int getRoleId() {
        return roleId;
    }

    public static void clearSession() {
        userId = null;
        roleId = 0;
    }

    public static String getCurrentUserId() { return currentUserId;
    }
}
