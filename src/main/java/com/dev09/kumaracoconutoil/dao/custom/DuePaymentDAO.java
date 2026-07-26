package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;

import java.util.ArrayList;

public interface DuePaymentDAO extends SuperDAO, CrudDAO<DuePaymentEntity> {
    ArrayList<DuePaymentEntity> getDuePaymentsByCustomerId(String customerId);
    boolean deleteByOrderId(String orderId);
    boolean deleteDueAddedByOrderId(String orderId);
}
