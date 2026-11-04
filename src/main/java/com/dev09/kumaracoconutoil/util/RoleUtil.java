package com.dev09.kumaracoconutoil.util;

/**
 * Central role names and helpers.
 *
 * "casheer" is the current name for the client/cashier role. The old
 * "client" value is still accepted so existing databases keep working.
 */
public final class RoleUtil {

    public static final String ADMIN = "admin";
    public static final String MANAGER = "manager";
    public static final String CASHIER = "casheer";
    public static final String LEGACY_CLIENT = "client";

    private RoleUtil() {
    }

    public static boolean isAdmin(String role) {
        return ADMIN.equalsIgnoreCase(role);
    }

    public static boolean isManager(String role) {
        return MANAGER.equalsIgnoreCase(role);
    }

    /**
     * True for both the current "casheer" role and the legacy "client" role.
     */
    public static boolean isCashier(String role) {
        return CASHIER.equalsIgnoreCase(role) || LEGACY_CLIENT.equalsIgnoreCase(role);
    }

    /**
     * Returns the current role name for display/storage, mapping legacy
     * "client" accounts to "casheer".
     */
    public static String normalize(String role) {
        return isCashier(role) ? CASHIER : role;
    }
}
