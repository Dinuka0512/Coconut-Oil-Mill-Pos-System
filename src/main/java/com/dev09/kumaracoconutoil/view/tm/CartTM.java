package com.dev09.kumaracoconutoil.view.tm;

import javafx.scene.control.Button;

public class CartTM {
    private String code;
    private String description;
    private double qty;
    private double unitPrice;
    private double total;
    private Button btn;

    public CartTM() {}

    public CartTM(String code, String description, double qty, double unitPrice, double total, Button btn) {
        this.code = code;
        this.description = description;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.total = total;
        this.btn = btn;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Button getBtn() { return btn; }
    public void setBtn(Button btn) { this.btn = btn; }

    @Override
    public String toString() {
        return "CartTM{code=" + code + ", description=" + description + ", qty=" + qty + "}";
    }
}
