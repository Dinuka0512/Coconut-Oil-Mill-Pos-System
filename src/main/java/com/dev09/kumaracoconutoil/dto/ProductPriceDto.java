package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;

import java.time.LocalDateTime;

public class ProductPriceDto {
    private String id;
    private String productId;
    private String productName;
    private double price;
    private double stock;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public ProductPriceDto() {
    }

    public ProductPriceDto(String id, String productId, String productName, double price) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
    }

    public ProductPriceDto(String id, String productId, String productName, double price, double stock) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedInfo() {
        return AuditFormat.info(createdBy, createdAt);
    }

    public String getUpdatedInfo() {
        return AuditFormat.info(updatedBy, updatedAt);
    }

    @Override
    public String toString() {
        return "ProductPriceDto{id=" + id + ", productId=" + productId + ", price=" + price + ", stock=" + stock + "}";
    }
}
