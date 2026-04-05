package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.dto.UserDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    public static UserDto userData;

    @FXML
    private AnchorPane body;

    @FXML
    private AnchorPane bodyContent;

    @FXML
    private Label lblUserEmail;

    @FXML
    private Label lblUserName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //--- set the user name and the email...
        lblUserEmail.setText(userData.getEmail());
        lblUserName.setText(userData.getName());
    }

    @FXML
    void logOut(ActionEvent event) {
        mainNavigateTo("/view/login.fxml");
        userData = null;
    }


    private void mainNavigateTo(String path) {
        try {
            // Load Dashboard FXML
            AnchorPane dashboardPane = FXMLLoader.load(getClass().getResource(path));

            // Clear current content
            body.getChildren().clear();

            // Add dashboard to the pane
            body.getChildren().add(dashboardPane);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load...").show();
        }
    }

    private void subNavigateTo(String path) {
        try {
            // Load Dashboard FXML
            AnchorPane dashboardPane = FXMLLoader.load(getClass().getResource(path));

            // Clear current content
            bodyContent.getChildren().clear();

            // Add dashboard to the pane
            bodyContent.getChildren().add(dashboardPane);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load...").show();
        }
    }

    @FXML
    void goToSettings(ActionEvent event) {
        subNavigateTo("/view/settings.fxml");
    }

    @FXML
    void goToHome(ActionEvent event) {
        mainNavigateTo("/view/dashboard.fxml");
    }
}
