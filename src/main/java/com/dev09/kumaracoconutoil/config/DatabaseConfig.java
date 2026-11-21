package com.dev09.kumaracoconutoil.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Applies database settings written by the Windows installer.
 *
 * <p>The installer saves credentials to
 * {@code %LOCALAPPDATA%\Dilshan Oil Mill\database.properties}. When that file exists,
 * its connection settings override the values bundled in {@code hibernate.properties}.
 * This lets each machine use its own MySQL administrator credentials without rebuilding
 * the application.</p>
 */
public final class DatabaseConfig {

    private static final String CONFIG_DIR = "Dilshan Oil Mill";
    private static final String CONFIG_FILE = "database.properties";

    private DatabaseConfig() {
    }

    public static void applyOverrides(Properties properties) {
        Path config = configPath();
        if (config == null || !Files.isRegularFile(config)) {
            return;
        }

        Properties overrides = new Properties();
        try (InputStream in = Files.newInputStream(config)) {
            overrides.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Could not read database config " + config + ": " + e.getMessage());
            return;
        }

        copy(overrides, properties, "hibernate.connection.url");
        copy(overrides, properties, "hibernate.connection.username");
        copy(overrides, properties, "hibernate.connection.password");
    }

    private static void copy(Properties source, Properties target, String key) {
        String value = source.getProperty(key);
        if (value != null && !value.isBlank()) {
            target.setProperty(key, value);
        }
    }

    private static Path configPath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, CONFIG_DIR, CONFIG_FILE);
        }
        return Paths.get(System.getProperty("user.home"), "." + CONFIG_DIR, CONFIG_FILE);
    }
}
