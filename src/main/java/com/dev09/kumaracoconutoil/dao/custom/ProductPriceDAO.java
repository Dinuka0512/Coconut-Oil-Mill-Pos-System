package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.ProductPriceEntity;

import java.util.ArrayList;

public interface ProductPriceDAO extends SuperDAO, CrudDAO<ProductPriceEntity> {
    ArrayList<ProductPriceEntity> getByProductId(String productId);

    boolean syncPrices(String productId, ArrayList<ProductPriceEntity> prices);

    boolean deleteByProductId(String productId);
}
