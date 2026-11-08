package com.dev09.kumaracoconutoil.dao;

import com.dev09.kumaracoconutoil.dao.custom.impl.*;

public class DAOFactory {
    private static DAOFactory daoFactory;
    private DAOFactory(){}

    public static DAOFactory getInstance(){
        return (daoFactory == null)? daoFactory = new DAOFactory() : daoFactory;
    }

    public enum DAOType{
        CUSTOMER, ORDER, ORDER_PRODUCT_DETAIL, PRODUCT, TAX, USER, DUE_PAYMENT, USER_LOG
    }

    public SuperDAO getDAO(DAOType type){
        switch (type){
            case CUSTOMER:
                return new CustomerDAOImpl();
            case USER:
                return new UserDAOImpl();
            case ORDER:
                return new OrderDAOImpl();
            case TAX:
                return new TaxDAOImpl();
            case PRODUCT:
                return new ProductDAOImpl();
            case ORDER_PRODUCT_DETAIL:
                return new OrderProductDetailDAOImpl();
            case DUE_PAYMENT:
                return new DuePaymentDAOImpl();
            case USER_LOG:
                return new UserLogDAOImpl();
            default:
                return null;
        }
    }
}
