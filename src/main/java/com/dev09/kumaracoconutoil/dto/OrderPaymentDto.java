package com.dev09.kumaracoconutoil.dto;

public class OrderPaymentDto {

    private String paymentId;
    private String orderId;
    private String method;
    private double amount;
    private String bankId;
    private String bankName;
    private String checkNo;

    public OrderPaymentDto() {
    }

    public OrderPaymentDto(String paymentId, String orderId, String method, double amount,
                           String bankId, String bankName, String checkNo) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.method = method;
        this.amount = amount;
        this.bankId = bankId;
        this.bankName = bankName;
        this.checkNo = checkNo;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getCheckNo() { return checkNo; }
    public void setCheckNo(String checkNo) { this.checkNo = checkNo; }

    @Override
    public String toString() {
        return "OrderPaymentDto{method=" + method + ", amount=" + amount
                + ", bankId=" + bankId + ", checkNo=" + checkNo + "}";
    }
}
