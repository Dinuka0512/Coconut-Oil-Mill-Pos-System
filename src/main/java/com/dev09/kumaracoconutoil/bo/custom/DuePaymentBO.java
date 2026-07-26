package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;

import java.util.ArrayList;

public interface DuePaymentBO extends SuperBo {
    boolean saveDuePayment(DuePaymentEntity entity);
    ArrayList<DuePaymentEntity> getAllDuePayments();
    ArrayList<DuePaymentEntity> getDuePaymentsByCustomerId(String customerId);
    boolean deleteByOrderId(String orderId);
    boolean deleteDueAddedByOrderId(String orderId);
}
