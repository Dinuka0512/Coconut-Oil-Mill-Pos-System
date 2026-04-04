package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.OrderProductDetailDAO;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;

import java.util.ArrayList;

public class OrderProductDetailDAOImpl implements OrderProductDetailDAO {
    @Override
    public ArrayList<OrderProductDetailsEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(OrderProductDetailsEntity dto) {
        return false;
    }

    @Override
    public boolean update(OrderProductDetailsEntity dto) {
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
    public OrderProductDetailsEntity search(String id) {
        return null;
    }
}
