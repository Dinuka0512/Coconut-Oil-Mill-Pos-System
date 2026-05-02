package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.util.Validation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Optional;

public class SettingsController {

    @FXML private AnchorPane body;
    @FXML private PasswordField txtCPassword;
    @FXML private TextField txtEmail;
    @FXML private TextField txtName;
    @FXML private PasswordField txtNewPassword;
    @FXML
    public static AnchorPane dashboardTemp;

    private final UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.BOType.USER);
    private UserDto currentUser;

    public static void dashboardTemp(AnchorPane body) {
        dashboardTemp = body;
    }

    @FXML
    public void initialize() {
        currentUser = DashboardController.userData;
        if (currentUser != null) {
            txtName.setText(currentUser.getName());
            txtEmail.setText(currentUser.getEmail());
        }
    }

    @FXML
    void updateUser(ActionEvent event) {
        if (currentUser == null) {
            new Alert(Alert.AlertType.ERROR, "No user logged in!").show();
            return;
        }

        String newName = txtName.getText().trim();
        String newEmail = txtEmail.getText().trim();
        String currentPasswordInput = txtCPassword.getText();
        String newPassword = txtNewPassword.getText();

        if (newName.isEmpty() || newEmail.isEmpty() || currentPasswordInput.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Name, Email, and Current Password are required!").show();
            return;
        }

        if (!currentUser.getPassword().equals(currentPasswordInput)) {
            new Alert(Alert.AlertType.ERROR, "Current password is incorrect!").show();
            return;
        }

        if (!"U001".equals(currentUser.getId())) {
            if (!Validation.isValidEmail(newEmail)) {
                new Alert(Alert.AlertType.WARNING, "Invalid email format!").show();
                return;
            }
        }

        if (userBo.isEmailExist(newEmail, currentUser.getId())) {
            new Alert(Alert.AlertType.WARNING, "Email already exists!").show();
            return;
        }

        if (userBo.isNameExist(newName, currentUser.getId())) {
            new Alert(Alert.AlertType.WARNING, "Username already exists!").show();
            return;
        }

        if (!newPassword.isEmpty() && newPassword.length() < 5) {
            new Alert(Alert.AlertType.WARNING, "Password must be at least 5 characters!").show();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Update Profile");
        confirmAlert.setHeaderText("Are you sure you want to update your profile?");
        confirmAlert.setContentText("Your changes will be saved.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String finalPassword = newPassword.isEmpty() ? currentUser.getPassword() : newPassword;
        UserDto updatedUser = new UserDto(
                currentUser.getId(),
                newName,
                newEmail,
                finalPassword,
                currentUser.getRole()
        );

        boolean success = userBo.updateUser(updatedUser);
        if (success) {
            DashboardController.userData = updatedUser;
            currentUser = updatedUser;
            mainNavigateTo("/view/login.fxml");
            new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!").show();
            clearPasswordFields();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update profile!").show();
        }
    }

    private void clearPasswordFields() {
        txtCPassword.clear();
        txtNewPassword.clear();
    }

    private void mainNavigateTo(String path) {
        try {
            AnchorPane dashboardPane = FXMLLoader.load(getClass().getResource(path));
            dashboardTemp.getChildren().clear();
            dashboardTemp.getChildren().add(dashboardPane);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load...").show();
        }
    }
}