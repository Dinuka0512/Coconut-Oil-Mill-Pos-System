package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.util.PriceQtyArray;

import java.util.ArrayList;

public class ProductBOImpl implements ProductBO {

    private final ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.PRODUCT);

    private ProductDto toDto(ProductEntity entity) {
        ProductDto dto = new ProductDto(
                entity.getId(),
                entity.getItemcode(),
                entity.getName(),
                entity.getPriceQtyPairs()
        );
        dto.setSupplierId(entity.getSupplierId());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDeleted(entity.isDeleted());
        return dto;
    }

    @Override
    public ArrayList<ProductDto> getAllProducts() {
        ArrayList<ProductDto> dtoList = new ArrayList<>();
        for (ProductEntity entity : productDAO.getAll()) {
            dtoList.add(toDto(entity));
        }
        return dtoList;
    }

    @Override
    public boolean saveProduct(ProductDto dto) {
        // A soft-deleted product still owns its unique item code, so instead of
        // inserting a conflicting row we restore that product with the new details.
        ProductEntity existing = productDAO.findAnyByItemCode(dto.getItemcode());
        if (existing != null) {
            if (!existing.isDeleted()) {
                return false; // genuine duplicate; the controller blocks this first
            }
            existing.setName(dto.getName());
            existing.setPriceQtyPairs(dto.getPriceQty());
            existing.setSupplierId(dto.getSupplierId());
            existing.setDeleted(false);
            return productDAO.revive(existing);
        }

        ProductEntity entity = new ProductEntity();
        entity.setId(dto.getId());
        entity.setItemcode(dto.getItemcode());
        entity.setName(dto.getName());
        entity.setPriceQtyPairs(dto.getPriceQty());
        entity.setSupplierId(dto.getSupplierId());
        return productDAO.save(entity);
    }

    @Override
    public boolean existActiveProductByItemCode(String itemCode) {
        return productDAO.existActiveByItemCode(itemCode);
    }

    @Override
    public String findAnyProductIdByItemCode(String itemCode) {
        ProductEntity entity = productDAO.findAnyByItemCode(itemCode);
        return entity == null ? null : entity.getId();
    }

    @Override
    public boolean updateProduct(ProductDto dto) {
        ProductEntity entity = productDAO.search(dto.getId());
        if (entity == null) return false;
        entity.setItemcode(dto.getItemcode());
        entity.setName(dto.getName());
        entity.setPriceQtyPairs(dto.getPriceQty());
        return productDAO.update(entity);
    }

    @Override
    public boolean existProductByItemCode(String itemCode) {
        return productDAO.existByItemCode(itemCode);
    }

    @Override
    public boolean existProduct(String id) {
        return productDAO.exist(id);
    }

    @Override
    public boolean deleteProduct(String id) {
        return productDAO.delete(id);
    }

    @Override
    public String generateNewProductId() {
        return productDAO.generateNewId();
    }

    @Override
    public ProductDto searchProduct(String id) {
        ProductEntity entity = productDAO.search(id);
        return entity == null ? null : toDto(entity);
    }

    @Override
    public ArrayList<ProductEntity> getAllProductsEntity() {
        return productDAO.getAll();
    }

    @Override
    public boolean updateStock(String productId, double quantity) {
        ProductEntity entity = productDAO.search(productId);
        if (entity == null) return false;
        entity.setQty(entity.getQty() - quantity);
        return productDAO.update(entity);
    }

    /** Append a new price/qty pair to a saved product. */
    @Override
    public boolean addPriceQty(String productId, double price, double qty) {
        ProductEntity entity = productDAO.search(productId);
        if (entity == null) return false;
        entity.setPriceQtyPairs(PriceQtyArray.append(entity.getPriceQtyPairs(), price, qty));
        return productDAO.update(entity);
    }

    @Override
    public ArrayList<ProductDto> getProductsBySupplier(String supplierId) {
        ArrayList<ProductDto> dtoList = new ArrayList<>();
        if (supplierId == null || supplierId.isBlank()) {
            return dtoList;
        }
        for (ProductEntity entity : productDAO.getBySupplier(supplierId)) {
            dtoList.add(toDto(entity));
        }
        return dtoList;
    }

    @Override
    public boolean assignSupplier(String productId, String supplierId) {
        ProductEntity entity = productDAO.search(productId);
        if (entity == null) return false;
        entity.setSupplierId(supplierId);
        return productDAO.update(entity);
    }
}
