package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.entity.UserEntity;

import java.util.ArrayList;

public class UserDAOImpl implements UserDAO {
    @Override
    public ArrayList<UserEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(UserEntity dto) {
        return false;
    }

    @Override
    public boolean update(UserEntity dto) {
        return false;
    }

    @Override
    public boolean exist(String id) {
        return false;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }

    @Override
    public String generateNewId() {
        return null;
    }

    @Override
    public UserEntity search(String id) {
        return null;
    }
}
