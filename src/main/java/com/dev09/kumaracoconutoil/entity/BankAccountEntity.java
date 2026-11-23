package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "bank_account")
@SQLDelete(sql = "UPDATE bank_account SET is_deleted = true WHERE bank_id = ?")
@SQLRestriction("is_deleted = false")
public class BankAccountEntity extends AuditableEntity {

    @Id
    @Column(name = "bank_id")
    private String bankId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "branch")
    private String branch;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public BankAccountEntity() {
    }

    public BankAccountEntity(String bankId, String bankName, String accountNumber,
                             String holderName, String branch, boolean isDeleted) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.branch = branch;
        this.isDeleted = isDeleted;
    }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "BankAccountEntity{bankId=" + bankId + ", bankName=" + bankName
                + ", accountNumber=" + accountNumber + ", holderName=" + holderName
                + ", branch=" + branch + "}";
    }
}
