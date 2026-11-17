package com.dev09.kumaracoconutoil.bo;

import com.dev09.kumaracoconutoil.bo.custom.impl.*;

public class BOFactory {
    private static BOFactory boFactory;
    private BOFactory(){}

    public static BOFactory getInstance(){
        return (boFactory == null) ? boFactory = new BOFactory() : boFactory;
    }

    public enum BOType {
        CUSTOMER, ORDER, ORDER_PRODUCT_DETAIL, PRODUCT, TAX, USER, DUE_PAYMENT, USER_LOG, SUPPLIER
    }

    public SuperBo getBo(BOType type){
        switch (type){
            case CUSTOMER:
                return new CustomerBOImpl();
            case ORDER:
                return new OrderBOImpl();
            case ORDER_PRODUCT_DETAIL:
                return new OrderProductDetailBOImpl();
            case PRODUCT:
                return new ProductBOImpl();
            case TAX:
                return new TaxBOImpl();
            case USER:
                return new UserBoImpl();
            case DUE_PAYMENT:
                return new DuePaymentBOImpl();
            case USER_LOG:
                return new UserLogBOImpl();
            case SUPPLIER:
                return new SupplierBOImpl();
            default:
                return null;
        }
    }
}
