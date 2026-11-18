package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.SupplierEntity;

public interface SupplierDAO extends SuperDAO, CrudDAO<SupplierEntity> {

    /** Insert the built-in supplier, or restore it if a row with that id already exists. */
    boolean ensureDefaultSupplier(String id, String name, String contact);
}
