package com.dev09.kumaracoconutoil;

import com.dev09.kumaracoconutoil.util.DatabaseBackupUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.InputStream;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Bebas Neue is bundled with the app, so target machines do not need the
        // font installed system-wide. Register it before any FXML is loaded.
        registerBundledFont();

        Parent root = FXMLLoader.load(getClass().getResource("/view/loading.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Dilshan Oil Mill");
        primaryStage.setResizable(false);

        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/asets/logo.png")));
        } catch (Exception e) {
            System.out.println("App icon not found");
        }

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (com.dev09.kumaracoconutoil.controller.DashboardController.userData != null) {
                    com.dev09.kumaracoconutoil.util.UserLogUtil.record("CLOSE_APP");
                }
            } catch (Exception ignored) {
            }
        });

        primaryStage.show();
        startAutomaticBackup();
    }

    private static void startAutomaticBackup() {
        Thread thread = new Thread(() -> {
            try {
                DatabaseBackupUtil.BackupResult result = DatabaseBackupUtil.backupDatabase();
                System.out.println("Automatic startup backup created: " + result.getFile().getName()
                        + " (" + result.getTableCount() + " tables, " + result.getRowCount() + " rows)");
            } catch (Exception e) {
                System.err.println("Automatic startup backup failed: " + e.getMessage());
            }
        }, "auto-db-backup");
        thread.setDaemon(true);
        thread.start();
    }

    private static void registerBundledFont() {
        try (InputStream stream = AppLauncher.class.getResourceAsStream("/fonts/BebasNeue-Regular.ttf")) {
            if (stream != null) {
                Font.loadFont(stream, 12);
            } else {
                System.err.println("Bundled Bebas Neue font not found in application resources.");
            }
        } catch (Exception e) {
            System.err.println("Could not register bundled Bebas Neue font: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
