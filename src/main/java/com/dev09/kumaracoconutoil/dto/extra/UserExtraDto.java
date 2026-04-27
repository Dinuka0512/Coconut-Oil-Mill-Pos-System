package com.dev09.kumaracoconutoil.dto.extra;

import com.dev09.kumaracoconutoil.dto.UserDto;

public class UserExtraDto {
    private UserDto userDto;
    private boolean isLogin;
    private String message;

    public UserExtraDto() {}

    public UserExtraDto(UserDto userDto, boolean isLogin, String message) {
        this.userDto = userDto;
        this.isLogin = isLogin;
        this.message = message;
    }

    public UserDto getUserDto() { return userDto; }
    public void setUserDto(UserDto userDto) { this.userDto = userDto; }
    public boolean isLogin() { return isLogin; }
    public void setLogin(boolean login) { isLogin = login; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
