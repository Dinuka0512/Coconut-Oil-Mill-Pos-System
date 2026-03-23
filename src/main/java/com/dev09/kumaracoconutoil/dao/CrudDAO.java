package com.dev09.kumaracoconutoil.dao;

import java.util.ArrayList;

public interface CrudDAO<T> {
    ArrayList<T> getAll();
    boolean save(T dto);
    boolean update(T dto);
    boolean exist(String id);
    boolean delete(String id);
    String generateNewId();
    T search(String id);
}
