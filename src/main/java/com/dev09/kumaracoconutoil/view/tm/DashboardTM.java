package com.dev09.kumaracoconutoil.view.tm;

public class DashboardTM {
    private String date;
    private String orderId;
    private String customer;
    private double qty;
    private double total;

    public DashboardTM() {}

    public DashboardTM(String date, String orderId, String customer, double qty, double total) {
        this.date = date;
        this.orderId = orderId;
        this.customer = customer;
        this.qty = qty;
        this.total = total;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}