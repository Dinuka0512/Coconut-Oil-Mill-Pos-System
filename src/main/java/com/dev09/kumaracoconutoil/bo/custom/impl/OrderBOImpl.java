package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderDAOImpl;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.util.PdfGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderBOImpl implements OrderBO {
    private final OrderDAO orderDAO = new OrderDAOImpl();

    @Override
    public boolean placeOrder(OrderDto dto) {
        return false;
    }

    @Override
    public String generateNextOrderId() throws SQLException, ClassNotFoundException {
        return orderDAO.generateNewId();
    }

    @Override
    public ArrayList<OrderDto> getAllOrders() throws SQLException, ClassNotFoundException {
        return new ArrayList<>();
    }

    @Override
    public boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException {
        return orderDAO.delete(orderId);
    }

    @Override
    public byte[] generateOrderBill(String orderId) throws SQLException, ClassNotFoundException {
        OrderEntity order = orderDAO.search(orderId);
        if (order == null) return null;

        List<OrderProductDetailsEntity> details = order.getOrderProducts();
        if (details == null) details = new ArrayList<>();

        return PdfGenerator.generateOrderBill(order, details);
    }
}