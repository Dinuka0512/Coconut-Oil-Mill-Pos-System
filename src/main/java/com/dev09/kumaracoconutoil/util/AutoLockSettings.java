package com.dev09.kumaracoconutoil.util;

import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Stores the auto-lock timeout chosen on the Admin Settings screen.
 *
 * <p>Saved next to the installer's database.properties in
 * {@code %LOCALAPPDATA%\Dilshan Oil Mill\appsettings.properties}, so each
 * machine keeps its own choice across restarts and updates.</p>
 */
public final class AutoLockSettings {

    public static final String NONE = "None";
    private static final String CONFIG_DIR = "Dilshan Oil Mill";
    private static final String CONFIG_FILE = "appsettings.properties";
    private static final String KEY_MINUTES = "autoLockMinutes";
    private static final int DEFAULT_MINUTES = 1;

    private AutoLockSettings() {
    }

    /** 0 = None (disabled), otherwise one of 1, 2, 5 or 10 minutes. */
    public static int getMinutes() {
        Path config = configPath();
        if (config != null && Files.isRegularFile(config)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(config)) {
                properties.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                int minutes = Integer.parseInt(properties.getProperty(KEY_MINUTES, "").trim());
                if (isValid(minutes)) {
                    return minutes;
                }
            } catch (IOException | NumberFormatException e) {
                // Fall through to the default.
            }
        }
        return DEFAULT_MINUTES;
    }

    public static void setMinutes(int minutes) {
        if (!isValid(minutes)) {
            return;
        }
        Path config = configPath();
        if (config == null) {
            return;
        }
        try {
            if (config.getParent() != null) {
                Files.createDirectories(config.getParent());
            }
            Properties properties = new Properties();
            if (Files.isRegularFile(config)) {
                try (InputStream in = Files.newInputStream(config)) {
                    properties.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
            properties.setProperty(KEY_MINUTES, String.valueOf(minutes));
            try (OutputStream out = Files.newOutputStream(config)) {
                properties.store(out, "Dilshan Oil Mill application settings");
            }
        } catch (IOException e) {
            System.err.println("Could not save auto-lock setting " + config + ": " + e.getMessage());
        }
    }

    public static Duration timeout() {
        int minutes = getMinutes();
        return minutes == 0 ? Duration.ZERO : Duration.minutes(minutes);
    }

    public static String label(int minutes) {
        return minutes == 0 ? NONE : minutes + " min";
    }

    public static String currentLabel() {
        return label(getMinutes());
    }

    public static int minutesFromLabel(String label) {
        if (label == null) {
            return DEFAULT_MINUTES;
        }
        String value = label.trim();
        if (NONE.equalsIgnoreCase(value)) {
            return 0;
        }
        try {
            int minutes = Integer.parseInt(value.replaceAll("[^0-9]", ""));
            return isValid(minutes) ? minutes : DEFAULT_MINUTES;
        } catch (NumberFormatException e) {
            return DEFAULT_MINUTES;
        }
    }

    public static boolean isValid(int minutes) {
        return minutes == 0 || minutes == 1 || minutes == 2 || minutes == 5 || minutes == 10;
    }

    private static Path configPath() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(localAppData, CONFIG_DIR, CONFIG_FILE);
        }
        return Paths.get(System.getProperty("user.home"), "." + CONFIG_DIR, CONFIG_FILE);
    }
}
