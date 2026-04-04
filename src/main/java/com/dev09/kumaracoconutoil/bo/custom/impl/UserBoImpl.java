package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;

public class UserBoImpl implements UserBo {
    private UserDAO userDao = (UserDAO) DAOFactory.getInstance().getDAO(DAOFactory.getDAOType.USER);

    @Override
    public void saveAdmin() {
        userDao.saveAdmin();
    }

    @Override
    public UserExtraDto login(String email, String password) {
        return userDao.login(email, password);
    }
}
