package com.dev09.kumaracoconutoil.dao.custom;

import com.dev09.kumaracoconutoil.dao.CrudDAO;
import com.dev09.kumaracoconutoil.dao.SuperDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;

import java.time.LocalDate;
import java.util.List;

public interface OrderDAO extends SuperDAO, CrudDAO<OrderEntity> {
    OrderEntity searchWithProducts(String orderId);
    List<LocalDate> getOrderDatesByMonth(int year, int month);
    double getTotalDueByCustomer(String customerId);
    List<OrderEntity> getUnpaidOrdersByCustomer(String customerId);
    List<OrderProductDetailsEntity> getOrderProductDetailsByDate(LocalDate date);
    int clearFullyPaidOrders();

    /** Dashboard aggregates - avoid loading every order/line into Java memory. */
    double getSalesBetween(LocalDate fromInclusive, LocalDate toExclusive);
    long getActiveCustomerCount();
    long getActiveProductCount();
    double getTotalCustomerDue();
    /** orderId, date, customerName, totalQty, salesTotal. */
    List<Object[]> getRecentSales(int limit);
    /** date, salesTotal. */
    List<Object[]> getSalesByDay(LocalDate fromInclusive, LocalDate toExclusive);
}
