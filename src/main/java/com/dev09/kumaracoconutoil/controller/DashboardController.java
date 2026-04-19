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
    private AnchorPane body;              // ← not static anymore

    @FXML
    private AnchorPane bodyContent;

    @FXML
    private Label lblUserEmail;

    @FXML
    private Label lblUserName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the user name and email
        updateUserDisplay();
    }

    // Call this method whenever userData changes
    public void updateUserDisplay() {
        if (userData != null) {
            lblUserName.setText(userData.getName());
            lblUserEmail.setText(userData.getEmail());
        }
    }

    @FXML
    void logOut(ActionEvent event) {
        mainNavigateTo("/view/login.fxml");
        userData = null;
    }

    private void mainNavigateTo(String path) {
        try {
            AnchorPane dashboardPane = FXMLLoader.load(getClass().getResource(path));
            body.getChildren().clear();
            body.getChildren().add(dashboardPane);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load...").show();
        }
    }

    private void subNavigateTo(String path) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(path));
            bodyContent.getChildren().clear();
            bodyContent.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load...").show();
        }
    }

    @FXML
    void goToSettings(ActionEvent event) {
        SettingsController.dashboardTemp(body);
        subNavigateTo("/view/settings.fxml");
    }

    @FXML
    void goToHome(ActionEvent event) {
        mainNavigateTo("/view/dashboard.fxml");
    }

    @FXML
    void goToCustomer(ActionEvent event) {
        subNavigateTo("/view/customers.fxml");
    }

    @FXML
    void goToInventory(ActionEvent event) {
        subNavigateTo("/view/inventory.fxml");
    }

    @FXML
    void goToOrder(ActionEvent event) {
        subNavigateTo("/view/pos.fxml");
    }
}