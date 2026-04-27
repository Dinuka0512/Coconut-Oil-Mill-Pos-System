package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String orderId;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    private double amountPaid;
    private double discount;
    private double tax;
    private double balance;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderProductDetailsEntity> orderProducts;

    public OrderEntity() {}

    public OrderEntity(String orderId, LocalDate date, CustomerEntity customer, double amountPaid,
                       double discount, double tax, double balance, boolean isDeleted) {
        this.orderId = orderId;
        this.date = date;
        this.customer = customer;
        this.amountPaid = amountPaid;
        this.discount = discount;
        this.tax = tax;
        this.balance = balance;
        this.isDeleted = isDeleted;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public List<OrderProductDetailsEntity> getOrderProducts() { return orderProducts; }
    public void setOrderProducts(List<OrderProductDetailsEntity> orderProducts) { this.orderProducts = orderProducts; }

    @Override
    public String toString() {
        return "OrderEntity{orderId=" + orderId + ", date=" + date + ", amountPaid=" + amountPaid + "}";
    }
}