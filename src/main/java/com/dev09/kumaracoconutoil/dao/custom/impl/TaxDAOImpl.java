package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.dao.custom.TaxDAO;
import com.dev09.kumaracoconutoil.entity.TaxEntity;

import java.util.ArrayList;

public class TaxDAOImpl implements TaxDAO {
    @Override
    public ArrayList<TaxEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(TaxEntity dto) {
        return false;
    }

    @Override
    public boolean update(TaxEntity dto) {
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
    public TaxEntity search(String id) {
        return null;
    }
}
