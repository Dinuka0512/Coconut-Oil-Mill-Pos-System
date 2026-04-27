package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;

public interface OrderDAO extends SuperDAO, CrudDAO<OrderEntity> {
    OrderEntity searchWithProducts(String orderId);
}
