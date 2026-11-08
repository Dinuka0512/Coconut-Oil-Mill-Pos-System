package com.dev09.kumaracoconutoil.util;

public final class AuditUtil {

    public static final String SYSTEM_USER = "system";

    private static String currentUser = SYSTEM_USER;
    private static String currentUserName = SYSTEM_USER;
    private static String currentUserRole = "";

    private AuditUtil() {
    }

    public static void setCurrentUser(String userId, String name) {
        setCurrentUser(userId, name, "");
    }

    public static void setCurrentUser(String userId, String name, String role) {
        currentUser = stamp(userId, name);
        currentUserName = name == null || name.trim().isEmpty() ? SYSTEM_USER : name.trim();
        currentUserRole = role == null ? "" : role.trim();
    }

    public static void clear() {
        currentUser = SYSTEM_USER;
        currentUserName = SYSTEM_USER;
        currentUserRole = "";
    }

    public static String currentUser() {
        if (currentUser == null || currentUser.isBlank()) {
            return SYSTEM_USER;
        }
        return currentUser;
    }

    public static String currentUserName() {
        if (currentUserName == null || currentUserName.isBlank()) {
            return SYSTEM_USER;
        }
        return currentUserName;
    }

    public static String currentUserRole() {
        return currentUserRole == null ? "" : currentUserRole;
    }

    private static String stamp(String userId, String name) {
        String id = userId == null ? "" : userId.trim();
        String userName = name == null ? "" : name.trim();

        if (id.isEmpty()) {
            return userName.isEmpty() ? SYSTEM_USER : userName;
        }
        if (userName.isEmpty()) {
            return id;
        }
        return id + "_" + userName;
    }
}
