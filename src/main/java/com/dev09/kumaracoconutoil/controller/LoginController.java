package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import com.dev09.kumaracoconutoil.util.SessionRestore;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.SupplierBO;
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
    private SupplierBO supplierBO = (SupplierBO) BOFactory.getInstance().getBo(BOFactory.BOType.SUPPLIER);

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
            com.dev09.kumaracoconutoil.dto.UserDto user = userExtraDto.getUserDto();
            DashboardController.userData = user;
            AuditUtil.setCurrentUser(user.getId(), user.getName(), user.getRole());
            com.dev09.kumaracoconutoil.util.UserLogUtil.record("LOGIN");
            com.dev09.kumaracoconutoil.util.UserLogUtil.purgeOlderLogs();

            // Only the account that was logged out by inactivity gets its work back.
            SessionRestore.Snapshot resume = SessionRestore.consumeFor(user);
            String home = SessionRestore.homeFxmlFor(user.getRole());
            String message = userExtraDto.getMessage()
                    + (resume != null ? "\n\nYour previous work has been restored." : "");
            AlertUtil.showAlert(Alert.AlertType.CONFIRMATION, message);
            mainNavigateTo(home, resume);
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

        // Default supplier (same idea as the Demo customer). Idempotent: restores
        // the row if it was soft-deleted instead of inserting a duplicate id.
        supplierBO.ensureDefaultSupplier(
                AppConstants.DEFAULT_SUPPLIER_ID,
                AppConstants.DEFAULT_SUPPLIER_NAME,
                AppConstants.DEFAULT_SUPPLIER_CONTACT);
    }

    private void mainNavigateTo(String path, SessionRestore.Snapshot resume) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            AnchorPane dashboardPane = loader.load();
            DashboardController dashboard = loader.getController();
            dashboard.setHomeFxml(path);

            body.getChildren().clear();
            body.getChildren().add(dashboardPane);

            if (resume != null) {
                dashboard.applyRestore(resume);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot load...");
        }
    }
}
