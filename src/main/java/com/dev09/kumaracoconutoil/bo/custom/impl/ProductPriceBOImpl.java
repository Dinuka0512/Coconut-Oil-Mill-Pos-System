package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.ProductPriceBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.ProductPriceDAO;
import com.dev09.kumaracoconutoil.dto.ProductPriceDto;
import com.dev09.kumaracoconutoil.entity.ProductPriceEntity;

import java.util.ArrayList;

public class ProductPriceBOImpl implements ProductPriceBO {

    private final ProductPriceDAO productPriceDAO = (ProductPriceDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.PRODUCT_PRICE);

    @Override
    public ArrayList<ProductPriceDto> getPricesByProduct(String productId) {
        ArrayList<ProductPriceDto> dtoList = new ArrayList<>();
        for (ProductPriceEntity entity : productPriceDAO.getByProductId(productId)) {
            dtoList.add(toDto(entity));
        }
        return dtoList;
    }

    @Override
    public ArrayList<ProductPriceDto> getAllPrices() {
        ArrayList<ProductPriceDto> dtoList = new ArrayList<>();
        for (ProductPriceEntity entity : productPriceDAO.getAll()) {
            dtoList.add(toDto(entity));
        }
        return dtoList;
    }

    @Override
    public boolean syncPrices(String productId, ArrayList<ProductPriceDto> prices) {
        ArrayList<ProductPriceEntity> entities = new ArrayList<>();
        for (ProductPriceDto dto : prices) {
            ProductPriceEntity entity = new ProductPriceEntity();
            entity.setId(dto.getId());
            entity.setProductId(productId);
            entity.setPrice(dto.getPrice());
            entity.setStock(dto.getStock());
            entities.add(entity);
        }
        return productPriceDAO.syncPrices(productId, entities);
    }

    @Override
    public boolean deletePricesByProduct(String productId) {
        return productPriceDAO.deleteByProductId(productId);
    }

    @Override
    public String generateNewPriceId() {
        return productPriceDAO.generateNewId();
    }

    private ProductPriceDto toDto(ProductPriceEntity entity) {
        ProductPriceDto dto = new ProductPriceDto(
                entity.getId(),
                entity.getProductId(),
                null,
                entity.getPrice(),
                entity.getStock()
        );
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
