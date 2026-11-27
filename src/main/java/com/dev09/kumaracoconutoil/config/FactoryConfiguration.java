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
            Configuration config = new Configuration();
            Properties bundled = new Properties();
            try (InputStream in = FactoryConfiguration.class.getResourceAsStream("/hibernate.properties")) {
                if (in != null) {
                    bundled.load(in);
                }
            } catch (Exception ignored) {
            }
            config.addProperties(bundled);
            config.addAnnotatedClass(UserEntity.class)
                    .addAnnotatedClass(OrderEntity.class)
                    .addAnnotatedClass(OrderProductDetailsEntity.class)
                    .addAnnotatedClass(ProductEntity.class)
                    .addAnnotatedClass(TaxEntity.class)
                    .addAnnotatedClass(CustomerEntity.class)
                    .addAnnotatedClass(DuePaymentEntity.class)
                    .addAnnotatedClass(UserLogEntity.class)
                    .addAnnotatedClass(SupplierEntity.class)
                    .addAnnotatedClass(BankAccountEntity.class)
                    .addAnnotatedClass(OrderPaymentEntity.class);
            DatabaseConfig.applyOverrides(config.getProperties());
            ensureSchema(config.getProperties());
            sessionFactory = config.buildSessionFactory();
        } catch (Exception e) {
            System.err.println("Failed to initialize Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();
            String detail = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                detail = e.getCause().getMessage();
            }
            throw new RuntimeException("Cannot initialize database connection: " + detail, e);
        }
    }

    /**
     * hibernate.hbm2ddl.auto is "validate", so the product.price_qty column must
     * exist before the SessionFactory is built. Schema setup and the one-time
     * legacy price migration are guarded by a version marker so they do not run
     * on every application start.
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

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS app_meta "
                    + "(meta_key VARCHAR(64) PRIMARY KEY, meta_value VARCHAR(64))");

            boolean initialized;
            try (ResultSet marker = statement.executeQuery(
                    "SELECT meta_value FROM app_meta WHERE meta_key = 'schema_version'")) {
                initialized = marker.next();
            }

            if (!initialized) {
                // Fresh installations have no tables yet (hbm2ddl.auto=validate), so
                // create them from the bundled schema before Hibernate validates.
                runSchemaScript(statement);

                if (!columnExists(connection, "product", "price_qty")) {
                    statement.executeUpdate("ALTER TABLE product ADD COLUMN price_qty TEXT");
                }
                migrateLegacyPrices(connection, statement);

                statement.executeUpdate("INSERT INTO app_meta (meta_key, meta_value) "
                        + "VALUES ('schema_version', '1')");
            }

            // Date-range reports are much faster with this index. It is safe to
            // create it once for databases that predate this change.
            if (!indexExists(connection, "orders", "idx_orders_date")) {
                try {
                    statement.executeUpdate("CREATE INDEX idx_orders_date ON orders (`date`)");
                } catch (SQLException ignored) {
                    // Table may not exist yet on a broken fresh install; Hibernate
                    // validation will report the real problem below.
                }
            }

            // Demo/walk-in bills can store the actual bill customer details.
            if (!columnExists(connection, "orders", "bill_customer_name")) {
                try {
                    statement.executeUpdate("ALTER TABLE orders ADD COLUMN bill_customer_name VARCHAR(255)");
                } catch (SQLException ignored) {
                }
            }
            if (!columnExists(connection, "orders", "bill_contact")) {
                try {
                    statement.executeUpdate("ALTER TABLE orders ADD COLUMN bill_contact VARCHAR(255)");
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("Could not prepare database schema: " + e.getMessage());
        }
    }

    private static boolean indexExists(Connection connection, String table, String index) throws SQLException {
        try (ResultSet indexes = connection.getMetaData().getIndexInfo(null, null, table, false, false)) {
            while (indexes.next()) {
                if (index.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
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
                if (sql.isEmpty()) {
                    continue;
                }
                try {
                    statement.execute(sql);
                } catch (Exception e) {
                    System.err.println("Schema statement skipped: " + e.getMessage());
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
