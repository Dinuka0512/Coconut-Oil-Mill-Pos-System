package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.ProductPriceDto;

import java.util.ArrayList;

public interface ProductPriceBO extends SuperBo {
    ArrayList<ProductPriceDto> getPricesByProduct(String productId);

    ArrayList<ProductPriceDto> getAllPrices();

    boolean syncPrices(String productId, ArrayList<ProductPriceDto> prices);

    boolean deletePricesByProduct(String productId);

    String generateNewPriceId();
}
