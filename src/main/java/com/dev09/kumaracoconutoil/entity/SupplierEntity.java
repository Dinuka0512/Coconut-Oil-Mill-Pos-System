package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "supplier")
@SQLDelete(sql = "UPDATE supplier SET is_deleted = true WHERE supplier_id = ?")
@SQLRestriction("is_deleted = false")
public class SupplierEntity extends AuditableEntity {

    @Id
    private String supplierId;

    private String name;
    private String contact;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public SupplierEntity() {}

    public SupplierEntity(String supplierId, String name, String contact, boolean isDeleted) {
        this.supplierId = supplierId;
        this.name = name;
        this.contact = contact;
        this.isDeleted = isDeleted;
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "SupplierEntity{supplierId=" + supplierId + ", name=" + name + ", contact=" + contact + "}";
    }
}
