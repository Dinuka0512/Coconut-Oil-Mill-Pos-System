package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.entity.ProductEntity;

import java.util.ArrayList;

public class ProductDAOImpl implements ProductDAO {
    @Override
    public ArrayList<ProductEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(ProductEntity dto) {
        return false;
    }

    @Override
    public boolean update(ProductEntity dto) {
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
    public ProductEntity search(String id) {
        return null;
    }
}
