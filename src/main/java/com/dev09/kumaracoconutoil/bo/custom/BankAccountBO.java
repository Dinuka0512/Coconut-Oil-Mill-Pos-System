package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.BankAccountDto;

import java.util.ArrayList;

public interface BankAccountBO extends SuperBo {
    ArrayList<BankAccountDto> getAllBankAccounts();
    boolean saveBankAccount(BankAccountDto dto);
    boolean updateBankAccount(BankAccountDto dto);
    boolean existBankAccount(String id);
    boolean deleteBankAccount(String id);
    String generateNewBankAccountId();
    BankAccountDto searchBankAccount(String id);
}
