package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.util.Validation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminSettingsController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField txtSearch;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private TableView<UserDto> tblUsers;
    @FXML private TableColumn<UserDto, String> colId;
    @FXML private TableColumn<UserDto, String> colName;
    @FXML private TableColumn<UserDto, String> colEmail;
    @FXML private TableColumn<UserDto, String> colPassword;
    @FXML private TableColumn<UserDto, String> colRole;

    private UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.BOType.USER);
    private String currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbRole.getItems().addAll("admin", "client");
        
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        pageReset();
        loadAllUsers();
        generateNextId();

        tblUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUserId = newValue.getId();
                txtName.setText(newValue.getName());
                txtEmail.setText(newValue.getEmail());
                txtPassword.setText(newValue.getPassword());
                cmbRole.getSelectionModel().select(newValue.getRole());

                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void loadAllUsers() {
        tblUsers.getItems().clear();
        ArrayList<UserDto> allUsers = userBo.getAllUsers();
        tblUsers.setItems(FXCollections.observableArrayList(allUsers));
    }

    private void generateNextId() {
        currentUserId = userBo.generateNewUserId();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            new Alert(Alert.AlertType.WARNING, "All fields are required!").show();
            return;
        }

        if (!Validation.isValidEmail(email)) {
            new Alert(Alert.AlertType.WARNING, "Invalid email format!").show();
            return;
        }

        if (userBo.isEmailExist(email, null)) {
            new Alert(Alert.AlertType.WARNING, "Email already exists!").show();
            return;
        }

        if (userBo.isNameExist(name, null)) {
            new Alert(Alert.AlertType.WARNING, "Username already exists!").show();
            return;
        }

        if (password.length() < 5) {
            new Alert(Alert.AlertType.WARNING, "Password must be at least 5 characters!").show();
            return;
        }

        UserDto newUser = new UserDto(currentUserId, name, email, password, role);
        boolean success = userBo.saveUser(newUser);
        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "User saved successfully!").show();
            loadAllUsers();
            clearFields();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save user!").show();
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            new Alert(Alert.AlertType.WARNING, "All fields are required!").show();
            return;
        }

        if (!"U001".equals(currentUserId)) {
            if (!Validation.isValidEmail(email)) {
                new Alert(Alert.AlertType.WARNING, "Invalid email format!").show();
                return;
            }
        }

        if (userBo.isEmailExist(email, currentUserId)) {
            new Alert(Alert.AlertType.WARNING, "Email already exists!").show();
            return;
        }

        if (userBo.isNameExist(name, currentUserId)) {
            new Alert(Alert.AlertType.WARNING, "Username already exists!").show();
            return;
        }

        if (password.length() < 5) {
            new Alert(Alert.AlertType.WARNING, "Password must be at least 5 characters!").show();
            return;
        }

        UserDto updatedUser = new UserDto(currentUserId, name, email, password, role);
        boolean success = userBo.updateUser(updatedUser);
        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "User updated successfully!").show();
            loadAllUsers();
            clearFields();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update user!").show();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if ("U001".equals(currentUserId)) {
            new Alert(Alert.AlertType.WARNING, "Cannot delete the main admin account!").show();
            return;
        }

        if (DashboardController.userData != null && DashboardController.userData.getId().equals(currentUserId)) {
            new Alert(Alert.AlertType.WARNING, "You cannot delete your own account!").show();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmAlert.setContentText("User: " + txtName.getText() + " (" + currentUserId + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userBo.deleteUser(currentUserId);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "User deleted successfully!").show();
                loadAllUsers();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete user!").show();
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            performSearchWithAlert();
        } else {
            performSearch();
        }
    }

    private void performSearch() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllUsers();
            return;
        }

        ArrayList<UserDto> allUsers = userBo.getAllUsers();
        ObservableList<UserDto> filtered = FXCollections.observableArrayList();

        for (UserDto dto : allUsers) {
            if (dto.getId().toLowerCase().contains(searchTerm) ||
                dto.getName().toLowerCase().contains(searchTerm) ||
                dto.getEmail().toLowerCase().contains(searchTerm)) {
                filtered.add(dto);
            }
        }

        tblUsers.setItems(filtered);
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllUsers();
            return;
        }

        ArrayList<UserDto> allUsers = userBo.getAllUsers();
        UserDto found = null;

        for (UserDto dto : allUsers) {
            if (dto.getId().toLowerCase().equals(searchTerm) ||
                dto.getName().toLowerCase().contains(searchTerm) ||
                dto.getEmail().toLowerCase().contains(searchTerm)) {
                found = dto;
                break;
            }
        }

        if (found != null) {
            currentUserId = found.getId();
            txtName.setText(found.getName());
            txtEmail.setText(found.getEmail());
            txtPassword.setText(found.getPassword());
            cmbRole.getSelectionModel().select(found.getRole());
            tblUsers.getSelectionModel().select(found);
            tblUsers.scrollTo(found);

            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        } else {
            new Alert(Alert.AlertType.WARNING, "User not found!").show();
        }
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtName.clear();
        txtEmail.clear();
        txtPassword.clear();
        cmbRole.getSelectionModel().clearSelection();
        txtSearch.clear();
        tblUsers.getSelectionModel().clearSelection();
        loadAllUsers();
    }
}
