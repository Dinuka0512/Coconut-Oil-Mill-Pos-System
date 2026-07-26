package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.entity.OrderEntity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface OrderBO extends SuperBo {
    boolean placeOrder(OrderDto dto) throws SQLException, ClassNotFoundException;
    boolean updateOrder(OrderDto dto) throws SQLException, ClassNotFoundException;
    String generateNextOrderId() throws SQLException, ClassNotFoundException;
    ArrayList<OrderDto> getAllOrders() throws SQLException, ClassNotFoundException;
    boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException;
    byte[] generateOrderBill80mm(String orderId) throws SQLException, ClassNotFoundException;
    byte[] generateOrderBillA4(String orderId) throws SQLException, ClassNotFoundException;
    OrderEntity getOrderById(String orderId) throws SQLException, ClassNotFoundException;
    OrderEntity getOrderWithProducts(String orderId) throws SQLException, ClassNotFoundException;
    List<LocalDate> getOrderDatesByMonth(int year, int month) throws SQLException, ClassNotFoundException;
    double getTotalDueByCustomer(String customerId) throws SQLException, ClassNotFoundException;
    List<OrderEntity> getUnpaidOrdersByCustomer(String customerId) throws SQLException, ClassNotFoundException;
    double payCustomerDue(String customerId, double amount, String note) throws SQLException, ClassNotFoundException;
}
