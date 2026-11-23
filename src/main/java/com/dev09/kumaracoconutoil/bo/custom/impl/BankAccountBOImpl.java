package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.BankAccountBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.BankAccountDAO;
import com.dev09.kumaracoconutoil.dto.BankAccountDto;
import com.dev09.kumaracoconutoil.entity.BankAccountEntity;

import java.util.ArrayList;

public class BankAccountBOImpl implements BankAccountBO {

    private final BankAccountDAO bankAccountDAO = (BankAccountDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.BANK_ACCOUNT);

    @Override
    public ArrayList<BankAccountDto> getAllBankAccounts() {
        ArrayList<BankAccountEntity> all = bankAccountDAO.getAll();
        ArrayList<BankAccountDto> accounts = new ArrayList<>();
        if (all == null) return accounts;
        for (BankAccountEntity entity : all) {
            accounts.add(toDto(entity));
        }
        return accounts;
    }

    @Override
    public boolean saveBankAccount(BankAccountDto dto) {
        return bankAccountDAO.save(new BankAccountEntity(
                dto.getBankId(), dto.getBankName(), dto.getAccountNumber(),
                dto.getHolderName(), dto.getBranch(), false));
    }

    @Override
    public boolean updateBankAccount(BankAccountDto dto) {
        return bankAccountDAO.update(new BankAccountEntity(
                dto.getBankId(), dto.getBankName(), dto.getAccountNumber(),
                dto.getHolderName(), dto.getBranch(), false));
    }

    @Override
    public boolean existBankAccount(String id) {
        return bankAccountDAO.exist(id);
    }

    @Override
    public boolean deleteBankAccount(String id) {
        return bankAccountDAO.delete(id);
    }

    @Override
    public String generateNewBankAccountId() {
        return bankAccountDAO.generateNewId();
    }

    @Override
    public BankAccountDto searchBankAccount(String id) {
        BankAccountEntity entity = bankAccountDAO.search(id);
        return entity == null ? null : toDto(entity);
    }

    private BankAccountDto toDto(BankAccountEntity entity) {
        BankAccountDto dto = new BankAccountDto(
                entity.getBankId(),
                entity.getBankName(),
                entity.getAccountNumber(),
                entity.getHolderName(),
                entity.getBranch()
        );
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
