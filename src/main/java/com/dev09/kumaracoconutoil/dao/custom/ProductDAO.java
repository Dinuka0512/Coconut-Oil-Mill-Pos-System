package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;

public interface ProductDAO extends SuperDAO, CrudDAO<ProductEntity> {
    boolean existByItemCode(String itemCode);

    /** Products that belong to the given supplier. */
    java.util.ArrayList<ProductEntity> getBySupplier(String supplierId);
}
