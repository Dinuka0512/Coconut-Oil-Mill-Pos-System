package com.dev09.kumaracoconutoil.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ProductDto {
    private String id;
    private String name;
    private double qty;
    private double price;
}
