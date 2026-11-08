package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.UserLogEntity;

import java.time.LocalDateTime;

public interface UserLogDAO extends SuperDAO, CrudDAO<UserLogEntity> {
    int deleteOlderThan(LocalDateTime cutoff);
}
