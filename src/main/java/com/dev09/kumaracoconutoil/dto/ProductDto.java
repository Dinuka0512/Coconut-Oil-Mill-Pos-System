package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;

import java.time.LocalDateTime;

public class ProductDto {
    private String id;
    private String itemcode;
    private String name;
    private double qty;
    private double price;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public ProductDto() {}

    public ProductDto(String id, String itemcode, String name, double qty, double price) {
        this.id = id;
        this.itemcode = itemcode;
        this.name = name;
        this.qty = qty;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemcode() { return itemcode; }
    public void setItemcode(String itemcode) { this.itemcode = itemcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
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
        return "ProductDto{id=" + id + ", itemcode=" + itemcode + ", name=" + name + ", qty=" + qty + ", price=" + price + "}";
    }
}
