package com.dev09.kumaracoconutoil.config;

import com.dev09.kumaracoconutoil.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {
    private SessionFactory sessionFactory;
    private static FactoryConfiguration factoryConfiguration;

    private FactoryConfiguration(){

        Configuration config = new Configuration()
                .addAnnotatedClass(UserEntity.class)
                .addAnnotatedClass(OrderEntity.class)
                .addAnnotatedClass(OrderProductDetails.class)
                .addAnnotatedClass(ProductEntity.class)
                .addAnnotatedClass(TaxEntity.class)
                .addAnnotatedClass(CustomerEntity.class);
        sessionFactory = config.buildSessionFactory();
    }

    public static FactoryConfiguration getInstance(){return (factoryConfiguration==null)? factoryConfiguration= new FactoryConfiguration() : factoryConfiguration;}

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
