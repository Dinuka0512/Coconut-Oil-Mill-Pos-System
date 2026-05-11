package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;

import java.time.LocalDate;
import java.util.List;

public interface OrderDAO extends SuperDAO, CrudDAO<OrderEntity> {
    OrderEntity searchWithProducts(String orderId);
    List<LocalDate> getOrderDatesByMonth(int year, int month);
}
