package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CustomerEntity {
    @Id
    private String custId;
    private String name;
    private String contact;
}
