package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;

public interface UserDAO extends SuperDAO, CrudDAO<UserEntity> {
    void saveAdmin();

    UserExtraDto login(String email, String password);
}
