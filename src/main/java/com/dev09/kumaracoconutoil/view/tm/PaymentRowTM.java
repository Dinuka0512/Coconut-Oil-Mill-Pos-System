package com.dev09.kumaracoconutoil.view.tm;

import javafx.beans.property.SimpleStringProperty;

/** One row in the split-payment table on the payment screen. */
public class PaymentRowTM {

    private final SimpleStringProperty method = new SimpleStringProperty();
    private final SimpleStringProperty amount = new SimpleStringProperty();
    private final SimpleStringProperty detail = new SimpleStringProperty();
    private final SimpleStringProperty bankId = new SimpleStringProperty();
    private final SimpleStringProperty bankName = new SimpleStringProperty();
    private final SimpleStringProperty checkNo = new SimpleStringProperty();

    public PaymentRowTM(String method, String amount, String detail, String bankId,
                        String bankName, String checkNo) {
        this.method.set(method);
        this.amount.set(amount);
        this.detail.set(detail);
        this.bankId.set(bankId);
        this.bankName.set(bankName);
        this.checkNo.set(checkNo);
    }

    public String getMethod() { return method.get(); }
    public void setMethod(String value) { method.set(value); }
    public String getAmount() { return amount.get(); }
    public void setAmount(String value) { amount.set(value); }
    public String getDetail() { return detail.get(); }
    public void setDetail(String value) { detail.set(value); }
    public String getBankId() { return bankId.get(); }
    public void setBankId(String value) { bankId.set(value); }
    public String getBankName() { return bankName.get(); }
    public void setBankName(String value) { bankName.set(value); }
    public String getCheckNo() { return checkNo.get(); }
    public void setCheckNo(String value) { checkNo.set(value); }
}
