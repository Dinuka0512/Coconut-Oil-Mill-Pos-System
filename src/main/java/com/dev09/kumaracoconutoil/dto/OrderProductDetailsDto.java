package com.dev09.kumaracoconutoil.dto;

public class OrderProductDetailsDto {
    private String id;
    private OrderDto order;
    private ProductDto product;
    private double quantity;
    private double unitPrice;

    public OrderProductDetailsDto() {}

    public OrderProductDetailsDto(String id, OrderDto order, ProductDto product, double quantity, double unitPrice) {
        this.id = id;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public OrderDto getOrder() { return order; }
    public void setOrder(OrderDto order) { this.order = order; }
    public ProductDto getProduct() { return product; }
    public void setProduct(ProductDto product) { this.product = product; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    @Override
    public String toString() {
        return "OrderProductDetailsDto{id=" + id + ", product=" + product + ", quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}
