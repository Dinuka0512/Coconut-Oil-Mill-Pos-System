package com.dev09.kumaracoconutoil.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
