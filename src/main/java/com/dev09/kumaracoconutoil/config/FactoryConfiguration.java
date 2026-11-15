package com.dev09.kumaracoconutoil.config;

import com.dev09.kumaracoconutoil.entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

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
                    .addAnnotatedClass(ProductPriceEntity.class)
                    .addAnnotatedClass(TaxEntity.class)
                    .addAnnotatedClass(CustomerEntity.class)
                    .addAnnotatedClass(DuePaymentEntity.class)
                    .addAnnotatedClass(UserLogEntity.class)
                    .addAnnotatedClass(SupplierEntity.class);
            ensureSchema(config.getProperties());
            sessionFactory = config.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Failed to initialize Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize database connection. Check hibernate.properties configuration.", e);
        }
    }

    /**
     * Creates tables that were added after the original schema was created.
     * hibernate.hbm2ddl.auto is set to "validate", so new tables must exist
     * before the SessionFactory is built.
     */
    private void ensureSchema(Properties properties) {
        String url = properties.getProperty("hibernate.connection.url");
        String username = properties.getProperty("hibernate.connection.username");
        String password = properties.getProperty("hibernate.connection.password");

        if (url == null || username == null) {
            return;
        }

        String createProductPrice = """
                CREATE TABLE IF NOT EXISTS product_price (
                    id VARCHAR(255) NOT NULL,
                    product_id VARCHAR(255) NOT NULL,
                    price DOUBLE NOT NULL,
                    stock DOUBLE NOT NULL DEFAULT 0,
                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                    created_by VARCHAR(255),
                    created_at DATETIME(6),
                    updated_by VARCHAR(255),
                    updated_at DATETIME(6),
                    PRIMARY KEY (id),
                    INDEX idx_product_price_product (product_id)
                ) ENGINE=InnoDB
                """;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createProductPrice);
            // Migrate existing product_price tables that were created before per-price stock was introduced.
            statement.executeUpdate("ALTER TABLE product_price ADD COLUMN stock DOUBLE NOT NULL DEFAULT 0");
        } catch (Exception e) {
            System.err.println("Could not verify product_price table: " + e.getMessage());
        }
    }

    public static FactoryConfiguration getInstance(){return (factoryConfiguration==null)? factoryConfiguration= new FactoryConfiguration() : factoryConfiguration;}

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
