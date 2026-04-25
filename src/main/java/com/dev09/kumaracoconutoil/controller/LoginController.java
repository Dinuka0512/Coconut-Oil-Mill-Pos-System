package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.hibernate.Session;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private AnchorPane body;

    private UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.BOType.USER);

    @FXML
    void login(ActionEvent event) {
        if(emailField.getText().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Required The email.").show();
            return;
        }

        if(txtPassword.getText().isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Required The password.").show();
            return;
        }

        UserExtraDto userExtraDto = userBo.login(emailField.getText(), txtPassword.getText());
        if(userExtraDto == null){
            new Alert(Alert.AlertType.ERROR, "Login service error. Please try again.").show();
            return;
        }
        if(userExtraDto.isLogin()){
            DashboardController.userData = userExtraDto.getUserDto();
            new Alert(Alert.AlertType.CONFIRMATION, userExtraDto.getMessage()).show();
            mainNavigateTo("/view/dashboard.fxml");
        }else{
            new Alert(Alert.AlertType.WARNING, userExtraDto.getMessage()).show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //FIRST HERE SAVE THE USER...
        userBo.saveAdmin();
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
}
