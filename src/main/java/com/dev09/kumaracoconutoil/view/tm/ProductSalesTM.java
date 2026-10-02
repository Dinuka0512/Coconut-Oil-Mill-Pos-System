package com.dev09.kumaracoconutoil.view.tm;

public class ProductSalesTM {
    private String itemCode;
    private String name;
    private double qty;
    private double total;

    public ProductSalesTM() {}

    public ProductSalesTM(String itemCode, String name, double qty, double total) {
        this.itemCode = itemCode;
        this.name = name;
        this.qty = qty;
        this.total = total;
    }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    @Override
    public String toString() {
        return "ProductSalesTM{itemCode=" + itemCode + ", name=" + name + ", qty=" + qty + ", total=" + total + "}";
    }
}