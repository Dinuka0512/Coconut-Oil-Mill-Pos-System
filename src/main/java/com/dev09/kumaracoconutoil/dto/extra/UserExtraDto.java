package com.dev09.kumaracoconutoil.dto.extra;

import com.dev09.kumaracoconutoil.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserExtraDto {
    private UserDto userDto;
    private boolean isLogin;
    private String message;
}
