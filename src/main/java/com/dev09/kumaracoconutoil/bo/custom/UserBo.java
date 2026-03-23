package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;

public interface UserBo extends SuperBo {
    boolean login(String email, String password);
}
