package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoadingController {

    @FXML
    private Label lblStatus;

    @FXML
    private ImageView imgLogo;

    @FXML
    private Rectangle lineLoader;

    private Timeline lineAnimation;

    public void initialize() {
        try {
            try {
                Image logo = new Image(getClass().getResourceAsStream("/asets/logo.png"));
                if (imgLogo != null && logo != null) {
                    imgLogo.setImage(logo);
                    Rectangle clip = new Rectangle(imgLogo.getFitWidth(), imgLogo.getFitHeight());
                    clip.setArcWidth(20.0);
                    clip.setArcHeight(20.0);
                    imgLogo.setClip(clip);
                }
            } catch (Exception e) {
                System.out.println("Logo not found: " + e.getMessage());
            }

            startLineAnimation();
        } catch (Exception e) {
            System.out.println("Error in initialize: " + e.getMessage());
        }

        new Thread(() -> {
            try {
                updateStatus("Connecting to database...");
                updateStatus("Initializing Hibernate...");
                FactoryConfiguration.getInstance().getSession().close();

                updateStatus("Loading application...");
                updateStatus("Ready!");

                Platform.runLater(() -> {
                    if (lineAnimation != null) {
                        lineAnimation.stop();
                    }
                    openLoginScreen();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (lineAnimation != null) lineAnimation.stop();
                    updateStatus("Startup failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private void startLineAnimation() {
        // Sweeping line animation - slides across
        lineLoader.setWidth(150.0);
        TranslateTransition sweep = new TranslateTransition(Duration.millis(800), lineLoader);
        sweep.setFromX(-150);
        sweep.setToX(150);
        sweep.setCycleCount(Animation.INDEFINITE);
        sweep.setAutoReverse(true);
        sweep.play();
        lineAnimation = new Timeline();
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

    private void openLoginScreen() {
        try {
            Stage stage = (Stage) lblStatus.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dilshan Oil Mill");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/asets/logo.png")));
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
