package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserLogBO;
import com.dev09.kumaracoconutoil.dto.UserLogDto;

import java.time.LocalDateTime;

public final class UserLogUtil {

    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_AUTO_LOGOUT = "AUTO_LOGOUT";
    public static final String ACTION_CLOSE_APP = "CLOSE_APP";

    private UserLogUtil() {
    }

    public static void purgeOlderThanOneWeek() {
        try {
            UserLogBO bo = (UserLogBO) BOFactory.getInstance().getBo(BOFactory.BOType.USER_LOG);
            if (bo != null) bo.purgeLogsOlderThanOneWeek();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void record(String action) {
        if (action == null || action.trim().isEmpty()) return;
        try {
            String userName = AuditUtil.currentUserName();
            String role = AuditUtil.currentUserRole();
            if (userName == null || userName.trim().isEmpty() || AuditUtil.SYSTEM_USER.equals(userName)) {
                if (com.dev09.kumaracoconutoil.controller.DashboardController.userData != null) {
                    userName = com.dev09.kumaracoconutoil.controller.DashboardController.userData.getName();
                    role = String.valueOf(com.dev09.kumaracoconutoil.controller.DashboardController.userData.getRole());
                } else {
                    return;
                }
            }
            UserLogBO bo = (UserLogBO) BOFactory.getInstance().getBo(BOFactory.BOType.USER_LOG);
            if (bo == null) return;
            bo.saveUserLog(new UserLogDto(null, userName, role == null ? "" : role, action.trim(), LocalDateTime.now()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
