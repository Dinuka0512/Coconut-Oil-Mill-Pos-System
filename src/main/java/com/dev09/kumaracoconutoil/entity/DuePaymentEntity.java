package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "due_payment")
public class DuePaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private double amount;

    private String note;

    @Column(name = "type", columnDefinition = "VARCHAR(20) DEFAULT 'DUE_ADDED'")
    private String type;

    public DuePaymentEntity() {}

    public DuePaymentEntity(String id, LocalDate date, CustomerEntity customer, OrderEntity order, double amount, String note, String type) {
        this.id = id;
        this.date = date;
        this.customer = customer;
        this.order = order;
        this.amount = amount;
        this.note = note;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "DuePaymentEntity{id=" + id + ", date=" + date + ", amount=" + amount + ", type=" + type + ", note=" + note + "}";
    }
}
