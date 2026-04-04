package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.hibernate.Session;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private AnchorPane body;

    private UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.getBoType.USER);

    @FXML
    void login(ActionEvent event) {
        if(emailField.getText().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Required The email.").show();
            return;
        }

        if(passwordField.getText().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Required The password.").show();
            return;
        }

//        boolean isLogedIn = userBo.login(emailField.getText(), passwordField.getText());

//        if(isLogedIn){
//            new Alert(Alert.AlertType.CONFIRMATION, "Login Success").show();
//        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //FIRST HERE SAVE THE USER...
        userBo.saveAdmin();
    }
}
