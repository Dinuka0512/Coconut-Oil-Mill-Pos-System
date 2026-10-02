package com.dev09.kumaracoconutoil;

import com.dev09.kumaracoconutoil.util.DatabaseBackupUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/loading.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Dilshan Oil Mill");
        primaryStage.setResizable(false);
        
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/asets/logo.png")));
        } catch (Exception e) {
            System.out.println("App icon not found");
        }
        
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

    public static void main(String[] args) {
        launch(args);
    }
}
