package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDao;
import com.dev09.kumaracoconutoil.entity.UserEntity;

public interface UserDao extends CrudDAO<UserEntity>, SuperDao {
    boolean login(String email, String password);
}
