package com.dev09.kumaracoconutoil.config;

import com.dev09.kumaracoconutoil.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {
    private SessionFactory sessionFactory;
    private static FactoryConfiguration factoryConfiguration;

    private FactoryConfiguration(){
        try {
            Configuration config = new Configuration()
                    .addAnnotatedClass(UserEntity.class)
                    .addAnnotatedClass(OrderEntity.class)
                    .addAnnotatedClass(OrderProductDetailsEntity.class)
                    .addAnnotatedClass(ProductEntity.class)
                    .addAnnotatedClass(TaxEntity.class)
                    .addAnnotatedClass(CustomerEntity.class)
                    .addAnnotatedClass(DuePaymentEntity.class);
            sessionFactory = config.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Failed to initialize Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize database connection. Check hibernate.properties configuration.", e);
        }
    }

    public static FactoryConfiguration getInstance(){return (factoryConfiguration==null)? factoryConfiguration= new FactoryConfiguration() : factoryConfiguration;}

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
