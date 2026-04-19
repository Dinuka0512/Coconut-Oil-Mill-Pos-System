package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.TaxBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.TaxDAO;
import com.dev09.kumaracoconutoil.dto.TaxDto;
import com.dev09.kumaracoconutoil.entity.TaxEntity;

import java.util.ArrayList;

public class TaxBOImpl implements TaxBO {
    private TaxDAO taxDAO = (TaxDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.TAX);

    @Override
    public ArrayList<TaxDto> getAllTax() {
        ArrayList<TaxEntity> all = taxDAO.getAll();
        ArrayList<TaxDto> allTax = new ArrayList<>();
        for (TaxEntity tax : all) {
            allTax.add(new TaxDto(tax.getId(), tax.getTax()));
        }
        return allTax;
    }

    @Override
    public boolean saveTax(TaxDto dto) {
        return taxDAO.save(new TaxEntity(dto.getId(), dto.getTax()));
    }

    @Override
    public boolean updateTax(TaxDto dto) {
        return taxDAO.update(new TaxEntity(dto.getId(), dto.getTax()));
    }

    @Override
    public boolean existTax(String id) {
        return taxDAO.exist(id);
    }

    @Override
    public boolean deleteTax(String id) {
        return taxDAO.delete(id);
    }

    @Override
    public String generateNewTaxId() {
        return taxDAO.generateNewId();
    }

    @Override
    public TaxDto searchTax(String id) {
        TaxEntity tax = taxDAO.search(id);
        if (tax != null) {
            return new TaxDto(tax.getId(), tax.getTax());
        }
        return null;
    }
}