package com.dev09.kumaracoconutoil;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
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