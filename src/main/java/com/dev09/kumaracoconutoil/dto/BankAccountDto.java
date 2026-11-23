package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;

import java.time.LocalDateTime;

public class BankAccountDto {
    private String bankId;
    private String bankName;
    private String accountNumber;
    private String holderName;
    private String branch;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public BankAccountDto() {
    }

    public BankAccountDto(String bankId, String bankName, String accountNumber,
                          String holderName, String branch) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.branch = branch;
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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedInfo() { return AuditFormat.info(createdBy, createdAt); }
    public String getUpdatedInfo() { return AuditFormat.info(updatedBy, updatedAt); }

    @Override
    public String toString() {
        return "BankAccountDto{bankId=" + bankId + ", bankName=" + bankName
                + ", accountNumber=" + accountNumber + ", holderName=" + holderName
                + ", branch=" + branch + "}";
    }
}
