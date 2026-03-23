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
@Table(name = "user")
public class UserEntity {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
}
