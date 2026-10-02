package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;

import java.time.LocalDateTime;

public class CustomerDto {
    private String custId;
    private String name;
    private String contact;
    private double dueAmount;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public CustomerDto() {}

    public CustomerDto(String custId, String name, String contact) {
        this.custId = custId;
        this.name = name;
        this.contact = contact;
    }

    public CustomerDto(String custId, String name, String contact, double dueAmount) {
        this.custId = custId;
        this.name = name;
        this.contact = contact;
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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedInfo() { return AuditFormat.info(createdBy, createdAt); }
    public String getUpdatedInfo() { return AuditFormat.info(updatedBy, updatedAt); }

    @Override
    public String toString() {
        return "CustomerDto{custId=" + custId + ", name=" + name + ", contact=" + contact + ", dueAmount=" + dueAmount + "}";
    }
}
