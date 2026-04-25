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
    private int qty;
    private double price;
}
