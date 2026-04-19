package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.TaxDto;

import java.util.ArrayList;

public interface TaxBO extends SuperBo {
    ArrayList<TaxDto> getAllTax();
    boolean saveTax(TaxDto dto);
    boolean updateTax(TaxDto dto);
    boolean existTax(String id);
    boolean deleteTax(String id);
    String generateNewTaxId();
    TaxDto searchTax(String id);
}