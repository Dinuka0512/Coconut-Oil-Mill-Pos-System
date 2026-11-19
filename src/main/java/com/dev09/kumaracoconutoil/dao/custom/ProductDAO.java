package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;

public interface ProductDAO extends SuperDAO, CrudDAO<ProductEntity> {
    /** Any product with this item code, soft-deleted included. */
    boolean existByItemCode(String itemCode);

    /** Only products that are not soft-deleted. */
    boolean existActiveByItemCode(String itemCode);

    /** Any product with this item code (soft-deleted included), or null. */
    ProductEntity findAnyByItemCode(String itemCode);

    /** Restore a soft-deleted product and refresh its details in one native update. */
    boolean revive(ProductEntity entity);

    /** Products that belong to the given supplier. */
    java.util.ArrayList<ProductEntity> getBySupplier(String supplierId);
}
