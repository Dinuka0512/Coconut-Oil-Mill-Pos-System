package com.dev09.kumaracoconutoil.dao;

import com.dev09.kumaracoconutoil.dao.custom.impl.UserDaoImpl;

public class DAOFactory {
    private static DAOFactory daoFactory;
    private DAOFactory(){}

    public static DAOFactory getInstance(){
        return (daoFactory==null)? daoFactory = new DAOFactory() : daoFactory;
    }

    public enum getDaoType{
        USER
    }

    public SuperDao getDao(getDaoType type){
        switch (type){
            case USER -> {return new UserDaoImpl();}
            default -> {return null;}
        }
    }
}
