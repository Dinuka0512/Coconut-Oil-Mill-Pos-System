package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.OrderPaymentEntity;

import java.util.ArrayList;

public interface OrderPaymentDAO extends SuperDAO {
    boolean save(OrderPaymentEntity entity);
    ArrayList<OrderPaymentEntity> getByOrderId(String orderId);
}
