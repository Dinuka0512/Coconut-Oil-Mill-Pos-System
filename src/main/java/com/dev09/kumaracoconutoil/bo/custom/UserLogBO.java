package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.UserLogDto;

import java.util.ArrayList;

public interface UserLogBO extends SuperBo {
    boolean saveUserLog(UserLogDto dto);
    ArrayList<UserLogDto> getAllUserLogs();
    int purgeLogsOlderThanOneWeek();
}
