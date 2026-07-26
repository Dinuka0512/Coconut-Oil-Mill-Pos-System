package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.DuePaymentBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.DuePaymentDAO;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;

import java.util.ArrayList;

public class DuePaymentBOImpl implements DuePaymentBO {
    private final DuePaymentDAO duePaymentDAO = (DuePaymentDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.DUE_PAYMENT);

    @Override
    public boolean saveDuePayment(DuePaymentEntity entity) {
        return duePaymentDAO.save(entity);
    }

    @Override
    public ArrayList<DuePaymentEntity> getAllDuePayments() {
        return duePaymentDAO.getAll();
    }

    @Override
    public ArrayList<DuePaymentEntity> getDuePaymentsByCustomerId(String customerId) {
        return duePaymentDAO.getDuePaymentsByCustomerId(customerId);
    }

    @Override
    public boolean deleteByOrderId(String orderId) {
        return duePaymentDAO.deleteByOrderId(orderId);
    }

    @Override
    public boolean deleteDueAddedByOrderId(String orderId) {
        return duePaymentDAO.deleteDueAddedByOrderId(orderId);
    }
}
