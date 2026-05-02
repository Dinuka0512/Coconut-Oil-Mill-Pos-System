package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;

import java.util.ArrayList;

public class UserBoImpl implements UserBo {
    private UserDAO userDao = (UserDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.USER);

    @Override
    public void saveAdmin() {
        userDao.saveAdmin();
    }

    @Override
    public UserExtraDto login(String email, String password) {
        return userDao.login(email, password);
    }

    @Override
    public boolean updateUser(UserDto userDto) {
        UserEntity entity = new UserEntity();
        entity.setId(userDto.getId());
        entity.setName(userDto.getName());
        entity.setEmail(userDto.getEmail());
        entity.setPassword(userDto.getPassword());
        entity.setRole(userDto.getRole());
        return userDao.updateUser(entity);
    }

    @Override
    public ArrayList<UserDto> getAllUsers() {
        return userDao.getAllUsersDto();
    }

    @Override
    public boolean saveUser(UserDto userDto) {
        UserEntity entity = new UserEntity();
        entity.setId(userDto.getId());
        entity.setName(userDto.getName());
        entity.setEmail(userDto.getEmail());
        entity.setPassword(userDto.getPassword());
        entity.setRole(userDto.getRole());
        return userDao.saveUser(entity);
    }

    @Override
    public boolean deleteUser(String id) {
        return userDao.deleteUser(id);
    }

    @Override
    public String generateNewUserId() {
        return userDao.generateNewUserId();
    }

    @Override
    public boolean isEmailExist(String email, String excludeUserId) {
        return userDao.isEmailExist(email, excludeUserId);
    }

    @Override
    public boolean isNameExist(String name, String excludeUserId) {
        return userDao.isNameExist(name, excludeUserId);
    }

    @Override
    public UserExtraDto loginByName(String name, String password) {
        return userDao.loginByName(name, password);
    }
}