package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tax")
public class TaxEntity {
    @Id
    private int id;
    private double tax;

    public TaxEntity() {}

    public TaxEntity(int id, double tax) {
        this.id = id;
        this.tax = tax;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    @Override
    public String toString() {
        return "TaxEntity{id=" + id + ", tax=" + tax + "}";
    }
}