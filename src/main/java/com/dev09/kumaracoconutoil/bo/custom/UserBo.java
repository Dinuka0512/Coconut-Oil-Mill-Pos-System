package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;

import java.util.ArrayList;

public interface UserBo extends SuperBo {
    void saveAdmin();

    UserExtraDto login(String email, String password);
    boolean updateUser(UserDto userDto);
    ArrayList<UserDto> getAllUsers();
    boolean saveUser(UserDto userDto);
    boolean deleteUser(String id);
    String generateNewUserId();
    boolean isEmailExist(String email, String excludeUserId);
}
