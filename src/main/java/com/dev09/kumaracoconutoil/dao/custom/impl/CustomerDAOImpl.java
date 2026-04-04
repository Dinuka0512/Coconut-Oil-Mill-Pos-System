package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.CustomerDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;

import java.util.ArrayList;

public class CustomerDAOImpl implements CustomerDAO {
    @Override
    public ArrayList<CustomerEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(CustomerEntity dto) {
        return false;
    }

    @Override
    public boolean update(CustomerEntity dto) {
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
    public CustomerEntity search(String id) {
        return null;
    }
}
