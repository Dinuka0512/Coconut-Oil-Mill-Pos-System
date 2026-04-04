package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.dto.UserDto;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

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
}
