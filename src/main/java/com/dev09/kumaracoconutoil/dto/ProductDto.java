package com.dev09.kumaracoconutoil.dto;

public class ProductDto {
    private String id;
    private String name;
    private double qty;
    private double price;

    public ProductDto() {}

    public ProductDto(String id, String name, double qty, double price) {
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return "ProductDto{id=" + id + ", name=" + name + ", qty=" + qty + ", price=" + price + "}";
    }
}
