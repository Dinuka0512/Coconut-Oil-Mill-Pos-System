package com.dev09.kumaracoconutoil;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
        // JavaFX 21 logs harmless CSS lookup warnings from its built-in Modena
        // stylesheet (JDK-8268657). Keep the console clean without hiding real errors.
        java.util.logging.Logger.getLogger("javafx.scene.CssStyleHelper")
                .setLevel(java.util.logging.Level.SEVERE);

        try {
            // Use reflection to avoid direct JavaFX dependency at compile time
            Class<?> appClass = Class.forName("com.dev09.kumaracoconutoil.AppLauncher");
            Method mainMethod = appClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (Exception e) {
            System.err.println("Failed to launch application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}