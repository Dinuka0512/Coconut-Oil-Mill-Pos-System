package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;

import java.util.ArrayList;

public interface UserDAO extends SuperDAO, CrudDAO<UserEntity> {
    void saveAdmin();
    UserExtraDto login(String email, String password);
    boolean updateUser(UserEntity user);
    ArrayList<UserDto> getAllUsersDto();
    boolean saveUser(UserEntity user);
    boolean deleteUser(String id);
    String generateNewUserId();
    boolean isEmailExist(String email, String excludeUserId);
    boolean isNameExist(String name, String excludeUserId);
    UserExtraDto loginByName(String name, String password);
}