package com.dev09.kumaracoconutoil.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtil {

    public static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.show();
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(alert::close);
        }).start();
    }
}
