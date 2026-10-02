package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ProductEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(unique = true)
    private String itemcode;

    private String name;
    private double qty;
    private double price;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public ProductEntity() {}

    public ProductEntity(String id, String itemcode, String name, double qty, double price, boolean isDeleted) {
        this.id = id;
        this.itemcode = itemcode;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.isDeleted = isDeleted;
    }

    public ProductEntity(String id, String itemcode, String name, double qty, double price) {
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
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "ProductEntity{id=" + id + ", itemcode=" + itemcode + ", name=" + name + ", qty=" + qty + ", price=" + price + "}";
    }
}