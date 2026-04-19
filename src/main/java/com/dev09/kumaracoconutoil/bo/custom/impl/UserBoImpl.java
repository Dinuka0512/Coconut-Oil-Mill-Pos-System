package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;

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
}