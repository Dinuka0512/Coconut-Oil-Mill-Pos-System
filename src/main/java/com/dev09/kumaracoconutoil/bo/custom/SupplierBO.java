package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import com.dev09.kumaracoconutoil.entity.SupplierEntity;

import java.util.ArrayList;

public interface SupplierBO extends SuperBo {
    ArrayList<SupplierDto> getAllSuppliers();
    boolean ensureDefaultSupplier(String id, String name, String contact);
    boolean saveSupplier(SupplierDto dto);
    boolean updateSupplier(SupplierDto dto);
    boolean existSupplier(String id);
    boolean deleteSupplier(String id);
    String generateNewSupplierId();
    SupplierDto searchSupplier(String id);
    ArrayList<SupplierEntity> getAllSuppliersEntity();
}
