package com.dev09.kumaracoconutoil.config;

import com.dev09.kumaracoconutoil.entity.*;
import com.dev09.kumaracoconutoil.util.PriceQtyArray;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
     * hibernate.hbm2ddl.auto is "validate", so the product.price_qty column must
     * exist before the SessionFactory is built. Existing price/stock data is
     * migrated into it once from the legacy columns/table.
     */
    private void ensureSchema(Properties properties) {
        String url = properties.getProperty("hibernate.connection.url");
        String username = properties.getProperty("hibernate.connection.username");
        String password = properties.getProperty("hibernate.connection.password");

        if (url == null || username == null) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            // Fresh installations have no tables yet (hbm2ddl.auto=validate), so create
            // them from the bundled schema before Hibernate validates the mappings.
            runSchemaScript(statement);

            if (!columnExists(connection, "product", "price_qty")) {
                statement.executeUpdate("ALTER TABLE product ADD COLUMN price_qty TEXT");
            }
            migrateLegacyPrices(connection, statement);
        } catch (Exception e) {
            System.err.println("Could not prepare product price_qty column: " + e.getMessage());
        }
    }

    /** Creates the database/tables on a new machine; every statement is idempotent. */
    private static void runSchemaScript(Statement statement) {
        try (InputStream in = FactoryConfiguration.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                System.err.println("Bundled database schema (/db/schema.sql) not found.");
                return;
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String raw : script.split(";")) {
                String sql = raw.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not prepare database schema: " + e.getMessage());
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, null, table, column)) {
            return columns.next();
        }
    }

    private static void migrateLegacyPrices(Connection connection, Statement statement) {
        try {
            if (!columnExists(connection, "product", "price")) {
                return;
            }

            ArrayList<String[]> pending = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery(
                    "SELECT id, price, qty FROM product WHERE price_qty IS NULL OR price_qty = ''")) {
                while (rows.next()) {
                    String id = rows.getString("id");
                    double price = rows.getDouble("price");
                    double qty = rows.getDouble("qty");
                    pending.add(new String[]{id, PriceQtyArray.toJson(new double[][]{{price, qty}})});
                }
            }

            for (String[] row : pending) {
                String legacyJson = legacyPriceRowsJson(connection, row[0]);
                if (legacyJson != null) {
                    row[1] = legacyJson;
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE product SET price_qty = ? WHERE id = ?")) {
                    update.setString(1, row[1]);
                    update.setString(2, row[0]);
                    update.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not migrate legacy product prices: " + e.getMessage());
        }
    }

    /** Reads the old product_price rows if that legacy table still exists. */
    private static String legacyPriceRowsJson(Connection connection, String productId) {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT price, stock FROM product_price WHERE product_id = ? AND is_deleted = false ORDER BY id")) {
            query.setString(1, productId);
            try (ResultSet rows = query.executeQuery()) {
                ArrayList<double[]> pairs = new ArrayList<>();
                while (rows.next()) {
                    pairs.add(new double[]{rows.getDouble("price"), rows.getDouble("stock")});
                }
                return pairs.isEmpty() ? null : PriceQtyArray.toJson(pairs.toArray(new double[0][]));
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public static FactoryConfiguration getInstance(){return (factoryConfiguration==null)? factoryConfiguration= new FactoryConfiguration() : factoryConfiguration;}

    public Session getSession(){
        return sessionFactory.openSession();
    }
}
