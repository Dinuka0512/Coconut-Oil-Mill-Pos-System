package com.dev09.kumaracoconutoil.dto;

import java.time.LocalDate;
import java.util.List;

public class OrderDto {
    private String orderId;
    private LocalDate date;
    private CustomerDto customer;
    private List<OrderProductDetailsDto> orderProducts;
    private double amountPaid;
    private double discount;
    private double tax;
    private double taxRate; // Tax rate at time of order (e.g., 8.0 for 8%)
    private double balance;

    public OrderDto() {}

    public OrderDto(String orderId, LocalDate date, CustomerDto customer, List<OrderProductDetailsDto> orderProducts,
                    double amountPaid, double discount, double tax, double taxRate, double balance) {
        this.orderId = orderId;
        this.date = date;
        this.customer = customer;
        this.orderProducts = orderProducts;
        this.amountPaid = amountPaid;
        this.discount = discount;
        this.tax = tax;
        this.taxRate = taxRate;
        this.balance = balance;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public CustomerDto getCustomer() { return customer; }
    public void setCustomer(CustomerDto customer) { this.customer = customer; }
    public List<OrderProductDetailsDto> getOrderProducts() { return orderProducts; }
    public void setOrderProducts(List<OrderProductDetailsDto> orderProducts) { this.orderProducts = orderProducts; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCustomerName() {
        return customer != null ? customer.getName() : "";
    }

    public String getContact() {
        return customer != null ? customer.getContact() : "";
    }

    public double getTotal() {
        return amountPaid;
    }

    @Override
    public String toString() {
        return "OrderDto{orderId=" + orderId + ", date=" + date + ", customer=" + customer + 
               ", amountPaid=" + amountPaid + ", discount=" + discount + ", tax=" + tax + ", balance=" + balance + "}";
    }
}
