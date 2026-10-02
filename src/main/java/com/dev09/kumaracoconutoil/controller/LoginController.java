package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
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
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private AnchorPane body;

    private UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.BOType.USER);
    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);

    @FXML
    void login(ActionEvent event) {
        if(txtUsername.getText().isEmpty()){
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Required The username.");
            return;
        }

        if(txtPassword.getText().isEmpty()){
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Required The password.");
            return;
        }

        UserExtraDto userExtraDto = userBo.loginByName(txtUsername.getText(), txtPassword.getText());
        if(userExtraDto == null){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Login service error. Please try again.");
            return;
        }
        if(userExtraDto.isLogin()){
            DashboardController.userData = userExtraDto.getUserDto();
            AuditUtil.setCurrentUser(userExtraDto.getUserDto().getId(), userExtraDto.getUserDto().getName());
            AlertUtil.showAlert(Alert.AlertType.CONFIRMATION, userExtraDto.getMessage());
            if ("client".equals(userExtraDto.getUserDto().getRole())) {
                mainNavigateTo("/view/clientDashboard.fxml");
            } else {
                mainNavigateTo("/view/dashboard.fxml");
            }
        }else{
            AlertUtil.showAlert(Alert.AlertType.WARNING, userExtraDto.getMessage());
        }
    }

    @FXML
    void onUsernameAction(ActionEvent event) {
        txtPassword.requestFocus();
    }

    @FXML
    void onPasswordAction(ActionEvent event) {
        login(event);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //FIRST HERE SAVE THE USER...
        userBo.saveAdmin();

        if (customerBO.getAllCustomers().isEmpty()) {
            customerBO.saveCustomer(new CustomerDto(AppConstants.DEMO_CUSTOMER_ID, "DEMO Customer", "democust@gmail.com"));
        }
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
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot load...");
        }
    }
}
