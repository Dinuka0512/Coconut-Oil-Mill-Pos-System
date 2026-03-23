package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name="customer")
public class CustomerEntity {
    @Id
    private String custId;
    private String name;
    private String contact;
}
