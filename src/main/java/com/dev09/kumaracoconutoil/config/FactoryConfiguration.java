package com.dev09.kumaracoconutoil.config;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactoryConfiguration {
    private SessionFactory sessionFactory;
    private static FactoryConfiguration factoryConfiguration;

    private FactoryConfiguration(){
        Configuration config = new Configuration();

        sessionFactory = config.buildSessionFactory();
    }

    public static FactoryConfiguration getInstance(){return (factoryConfiguration==null)? factoryConfiguration= new FactoryConfiguration() : factoryConfiguration;}

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
