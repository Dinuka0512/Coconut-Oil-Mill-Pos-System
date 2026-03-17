package com.dev09.kumaracoconutoil.bo;

import com.dev09.kumaracoconutoil.bo.custom.impl.UserBoImpl;

public class BOFactory {
    public static BOFactory boFactory;
    private BOFactory(){}

    public static  BOFactory getInstance(){return (boFactory==null)? boFactory = new BOFactory() : boFactory;}

    public enum getBoType{
        USER
    }

    public SuperBo getBo(getBoType type){
        switch (type){
            case USER -> {return new UserBoImpl();}
            default -> {return null;}
        }
    }
}
