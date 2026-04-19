package com.dev09.kumaracoconutoil.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
@SQLDelete(sql = "UPDATE customer SET is_deleted = true WHERE cust_id = ?")
@SQLRestriction("is_deleted = false")
public class CustomerEntity {

    @Id
    private String custId;

    private String name;
    private String contact;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderEntity> orders;
}