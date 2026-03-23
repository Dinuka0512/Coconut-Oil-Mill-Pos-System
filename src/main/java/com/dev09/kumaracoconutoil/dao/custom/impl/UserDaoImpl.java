package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.SuperDao;
import com.dev09.kumaracoconutoil.dao.custom.UserDao;
import com.dev09.kumaracoconutoil.entity.UserEntity;

import java.util.ArrayList;

public class UserDaoImpl implements UserDao {
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

    @Override
    public boolean login(String email, String password) {
        //check is exist
        if(exist(email)){
            //Email exist - Then check password

        }
        return false;
    }
}
