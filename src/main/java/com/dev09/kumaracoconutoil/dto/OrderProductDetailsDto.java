package com.dev09.kumaracoconutoil.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderProductDetailsDto {
    private String id;
    private OrderDto order;
    private ProductDto product;
    private double quantity;
    private double unitPrice;
}
