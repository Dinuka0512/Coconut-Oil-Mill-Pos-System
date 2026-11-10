package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.SupplierBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.SupplierDAO;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import com.dev09.kumaracoconutoil.entity.SupplierEntity;

import java.util.ArrayList;

public class SupplierBOImpl implements SupplierBO {
    private final SupplierDAO supplierDAO = (SupplierDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.SUPPLIER);

    @Override
    public ArrayList<SupplierDto> getAllSuppliers() {
        ArrayList<SupplierEntity> all = supplierDAO.getAll();
        ArrayList<SupplierDto> allSuppliers = new ArrayList<>();
        if (all == null) return allSuppliers;
        for (SupplierEntity supplier : all) {
            SupplierDto dto = new SupplierDto(
                    supplier.getSupplierId(),
                    supplier.getName(),
                    supplier.getContact()
            );
            dto.setCreatedBy(supplier.getCreatedBy());
            dto.setCreatedAt(supplier.getCreatedAt());
            dto.setUpdatedBy(supplier.getUpdatedBy());
            dto.setUpdatedAt(supplier.getUpdatedAt());
            allSuppliers.add(dto);
        }
        return allSuppliers;
    }

    @Override
    public boolean saveSupplier(SupplierDto dto) {
        SupplierEntity entity = new SupplierEntity(
                dto.getSupplierId(),
                dto.getName(),
                dto.getContact(),
                false
        );
        return supplierDAO.save(entity);
    }

    @Override
    public boolean updateSupplier(SupplierDto dto) {
        SupplierEntity entity = new SupplierEntity(
                dto.getSupplierId(),
                dto.getName(),
                dto.getContact(),
                false
        );
        return supplierDAO.update(entity);
    }

    @Override
    public boolean existSupplier(String id) {
        return supplierDAO.exist(id);
    }

    @Override
    public boolean deleteSupplier(String id) {
        return supplierDAO.delete(id);
    }

    @Override
    public String generateNewSupplierId() {
        return supplierDAO.generateNewId();
    }

    @Override
    public SupplierDto searchSupplier(String id) {
        SupplierEntity supplier = supplierDAO.search(id);
        if (supplier != null) {
            SupplierDto dto = new SupplierDto(
                    supplier.getSupplierId(),
                    supplier.getName(),
                    supplier.getContact()
            );
            dto.setCreatedBy(supplier.getCreatedBy());
            dto.setCreatedAt(supplier.getCreatedAt());
            dto.setUpdatedBy(supplier.getUpdatedBy());
            dto.setUpdatedAt(supplier.getUpdatedAt());
            return dto;
        }
        return null;
    }

    @Override
    public ArrayList<SupplierEntity> getAllSuppliersEntity() {
        return supplierDAO.getAll();
    }
}
