package com.dev09.kumaracoconutoil.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class SettingsController {
    @FXML
    private AnchorPane body;

    @FXML
    private PasswordField txtCPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtName;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    void updateUser(ActionEvent event) {
    }
}
