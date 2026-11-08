package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.UserLogBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.UserLogDAO;
import com.dev09.kumaracoconutoil.dto.UserLogDto;
import com.dev09.kumaracoconutoil.entity.UserLogEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserLogBOImpl implements UserLogBO {
    private final UserLogDAO userLogDAO = (UserLogDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.USER_LOG);

    @Override
    public boolean saveUserLog(UserLogDto dto) {
        if (dto == null) return false;
        return userLogDAO.save(new UserLogEntity(dto.getUserName(), dto.getRole(), dto.getAction(), dto.getLoggedAt()));
    }

    @Override
    public ArrayList<UserLogDto> getAllUserLogs() {
        ArrayList<UserLogEntity> entities = userLogDAO.getAll();
        ArrayList<UserLogDto> dtos = new ArrayList<>();
        if (entities == null) return dtos;
        for (UserLogEntity e : entities) {
            dtos.add(new UserLogDto(e.getId(), e.getUserName(), e.getRole(), e.getAction(), e.getLoggedAt()));
        }
        return dtos;
    }

    @Override
    public int purgeLogsOlderThanOneWeek() {
        return userLogDAO.deleteOlderThan(LocalDateTime.now().minusDays(7));
    }
}
