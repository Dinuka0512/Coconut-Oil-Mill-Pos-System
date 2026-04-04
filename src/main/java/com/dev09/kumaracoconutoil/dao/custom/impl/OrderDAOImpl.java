package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.entity.OrderEntity;

import java.util.ArrayList;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public ArrayList<OrderEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(OrderEntity dto) {
        return false;
    }

    @Override
    public boolean update(OrderEntity dto) {
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
    public OrderEntity search(String id) {
        return null;
    }
}
