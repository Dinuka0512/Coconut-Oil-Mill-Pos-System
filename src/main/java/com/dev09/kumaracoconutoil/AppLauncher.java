package com.dev09.kumaracoconutoil;

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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
