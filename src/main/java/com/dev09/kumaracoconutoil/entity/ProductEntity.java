package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.internal.build.AllowNonPortable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class ProductEntity {
    @Id
    private String id;
    private String name;
    private int qty;
    private double price;
}
