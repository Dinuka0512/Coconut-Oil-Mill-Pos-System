package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ProductEntity {

    @Id
    private String id;

    private String name;
    private int qty;
    private double price;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;
}