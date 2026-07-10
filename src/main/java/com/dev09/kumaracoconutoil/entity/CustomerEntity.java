package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "customer")
@SQLDelete(sql = "UPDATE customer SET is_deleted = true WHERE cust_id = ?")
@SQLRestriction("is_deleted = false")
public class CustomerEntity {

    @Id
    private String custId;

    private String name;
    private String contact;

    @Column(name = "due_amount", columnDefinition = "double default 0")
    private double dueAmount = 0;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderEntity> orders;

    public CustomerEntity() {}

    public CustomerEntity(String custId, String name, String contact, boolean isDeleted) {
        this.custId = custId;
        this.name = name;
        this.contact = contact;
        this.isDeleted = isDeleted;
    }

    public CustomerEntity(String custId, String name, String contact, boolean isDeleted, double dueAmount) {
        this.custId = custId;
        this.name = name;
        this.contact = contact;
        this.isDeleted = isDeleted;
        this.dueAmount = dueAmount;
    }

    public String getCustId() { return custId; }
    public void setCustId(String custId) { this.custId = custId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public List<OrderEntity> getOrders() { return orders; }
    public void setOrders(List<OrderEntity> orders) { this.orders = orders; }

    @Override
    public String toString() {
        return "CustomerEntity{custId=" + custId + ", name=" + name + ", contact=" + contact + "}";
    }
}