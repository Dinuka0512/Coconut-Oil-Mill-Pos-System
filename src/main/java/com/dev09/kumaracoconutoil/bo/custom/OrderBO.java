package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.OrderDto;

import java.sql.SQLException;
import java.util.ArrayList;

public interface OrderBO extends SuperBo {
    boolean placeOrder(OrderDto dto) throws SQLException, ClassNotFoundException;
    boolean updateOrder(OrderDto dto) throws SQLException, ClassNotFoundException;
    String generateNextOrderId() throws SQLException, ClassNotFoundException;
    ArrayList<OrderDto> getAllOrders() throws SQLException, ClassNotFoundException;
    boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException;
    byte[] generateOrderBill(String orderId) throws SQLException, ClassNotFoundException;
}
