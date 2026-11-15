package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "product_price")
@SQLDelete(sql = "UPDATE product_price SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ProductPriceEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    private double price;

    @Column(name = "stock", nullable = false)
    private double stock = 0;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public ProductPriceEntity() {
    }

    public ProductPriceEntity(String id, String productId, double price) {
        this.id = id;
        this.productId = productId;
        this.price = price;
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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return "ProductPriceEntity{id=" + id + ", productId=" + productId + ", price=" + price + ", stock=" + stock + "}";
    }
}
