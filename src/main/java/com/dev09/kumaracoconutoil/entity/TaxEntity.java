package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TaxEntity {
    @Id
    private int id;
    private double tax;
}