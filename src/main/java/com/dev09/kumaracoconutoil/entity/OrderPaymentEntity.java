package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;

/** One payment row belonging to an order (an order can have several). */
@Entity
@Table(name = "order_payment")
public class OrderPaymentEntity extends AuditableEntity {

    @Id
    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "method")
    private String method;

    @Column(name = "amount")
    private double amount;

    @Column(name = "bank_id")
    private String bankId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "check_no")
    private String checkNo;

    public OrderPaymentEntity() {
    }

    public OrderPaymentEntity(String paymentId, String orderId, String method, double amount,
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
        return "OrderPaymentEntity{paymentId=" + paymentId + ", orderId=" + orderId
                + ", method=" + method + ", amount=" + amount + "}";
    }
}
