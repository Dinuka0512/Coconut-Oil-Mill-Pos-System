package com.dev09.kumaracoconutoil.dto;

public class TaxDto {
    private int id;
    private double tax;

    public TaxDto() {}

    public TaxDto(int id, double tax) {
        this.id = id;
        this.tax = tax;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    @Override
    public String toString() {
        return "TaxDto{id=" + id + ", tax=" + tax + "}";
    }
}