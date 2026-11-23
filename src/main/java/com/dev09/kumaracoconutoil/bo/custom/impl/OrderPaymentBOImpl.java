package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.OrderPaymentBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.OrderPaymentDAO;
import com.dev09.kumaracoconutoil.dto.OrderPaymentDto;
import com.dev09.kumaracoconutoil.entity.OrderPaymentEntity;

import java.util.ArrayList;

public class OrderPaymentBOImpl implements OrderPaymentBO {

    private final OrderPaymentDAO orderPaymentDAO = (OrderPaymentDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.ORDER_PAYMENT);

    @Override
    public boolean savePayment(OrderPaymentDto dto) {
        return orderPaymentDAO.save(new OrderPaymentEntity(
                dto.getPaymentId(), dto.getOrderId(), dto.getMethod(), dto.getAmount(),
                dto.getBankId(), dto.getBankName(), dto.getCheckNo()));
    }

    @Override
    public ArrayList<OrderPaymentDto> getPaymentsByOrderId(String orderId) {
        ArrayList<OrderPaymentEntity> entities = orderPaymentDAO.getByOrderId(orderId);
        ArrayList<OrderPaymentDto> payments = new ArrayList<>();
        if (entities == null) return payments;
        for (OrderPaymentEntity entity : entities) {
            payments.add(new OrderPaymentDto(
                    entity.getPaymentId(), entity.getOrderId(), entity.getMethod(), entity.getAmount(),
                    entity.getBankId(), entity.getBankName(), entity.getCheckNo()));
        }
        return payments;
    }
}
