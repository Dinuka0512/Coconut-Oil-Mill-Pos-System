package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.util.PdfGenerator;

import java.util.ArrayList;

public class ProductBOImpl implements ProductBO {

    private final ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.PRODUCT);

    @Override
    public ArrayList<ProductDto> getAllProducts() {
        ArrayList<ProductEntity> all = productDAO.getAll();
        ArrayList<ProductDto> dtoList = new ArrayList<>();
        for (ProductEntity entity : all) {
            ProductDto dto = new ProductDto(
                    entity.getId(),
                    entity.getItemcode(),
                    entity.getName(),
                    entity.getQty(),
                    entity.getPrice()
            );
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedAt(entity.getUpdatedAt());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public boolean saveProduct(ProductDto dto) {
        // Create entity using setters (avoid constructor confusion)
        ProductEntity entity = new ProductEntity();
        entity.setId(dto.getId());
        entity.setItemcode(dto.getItemcode());
        entity.setName(dto.getName());
        entity.setQty(dto.getQty());
        entity.setPrice(dto.getPrice());
        // isDeleted defaults to false
        return productDAO.save(entity);
    }

    @Override
    public boolean updateProduct(ProductDto dto) {
        ProductEntity entity = productDAO.search(dto.getId());
        if (entity == null) return false;
        entity.setItemcode(dto.getItemcode());
        entity.setName(dto.getName());
        entity.setQty(dto.getQty());
        entity.setPrice(dto.getPrice());
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
        if (entity != null) {
            ProductDto dto = new ProductDto(
                    entity.getId(),
                    entity.getItemcode(),
                    entity.getName(),
                    entity.getQty(),
                    entity.getPrice()
            );
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedAt(entity.getUpdatedAt());
            return dto;
        }
        return null;
    }



    @Override
    public ArrayList<ProductEntity> getAllProductsEntity() {
        return productDAO.getAll();
    }

    @Override
    public boolean updateStock(String productId, double quantity) {
        ProductEntity entity = productDAO.search(productId);
        if (entity != null) {
            double newQty = entity.getQty() - quantity;
            entity.setQty(newQty);
            return productDAO.update(entity);
        }
        return false;
    }
}