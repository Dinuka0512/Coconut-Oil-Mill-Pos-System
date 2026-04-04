package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;

public interface UserBo extends SuperBo {
    void saveAdmin();

    UserExtraDto login(String email, String password);
}
