package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.UserDao;

public class UserBoImpl implements UserBo {
    private UserDao userDao = (UserDao) DAOFactory.getInstance().getDao(DAOFactory.getDaoType.USER);
    @Override
    public boolean login(String email, String password) {
        return userDao.login(email, password);
    }
}
