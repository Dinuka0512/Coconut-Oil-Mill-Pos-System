package com.dev09.kumaracoconutoil.util;

public final class AuditUtil {

    public static final String SYSTEM_USER = "system";

    private static String currentUser = SYSTEM_USER;

    private AuditUtil() {
    }

    public static void setCurrentUser(String userId, String name) {
        currentUser = stamp(userId, name);
    }

    public static void clear() {
        currentUser = SYSTEM_USER;
    }

    public static String currentUser() {
        if (currentUser == null || currentUser.isBlank()) {
            return SYSTEM_USER;
        }
        return currentUser;
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
