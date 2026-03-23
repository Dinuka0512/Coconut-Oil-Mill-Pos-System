package com.dev09.kumaracoconutoil.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String password;
}
