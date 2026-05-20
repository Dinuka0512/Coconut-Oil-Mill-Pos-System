package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.ProductDto;

import java.util.ArrayList;

public interface ProductBO extends SuperBo {
    ArrayList<ProductDto> getAllProducts();
    boolean saveProduct(ProductDto dto);
    boolean existProductByItemCode(String itemCode);
    boolean updateProduct(ProductDto dto);
    boolean existProduct(String id);
    boolean deleteProduct(String id);
    String generateNewProductId();
    ProductDto searchProduct(String id);

    ArrayList<com.dev09.kumaracoconutoil.entity.ProductEntity> getAllProductsEntity();
    boolean updateStock(String productId, double quantity);
}