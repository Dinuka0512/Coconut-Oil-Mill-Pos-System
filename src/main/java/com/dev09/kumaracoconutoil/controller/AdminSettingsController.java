package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditCell;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.UserBo;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import com.dev09.kumaracoconutoil.util.Validation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminSettingsController implements Initializable {

    private static final String MASKED_PASSWORD = "******";

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

    @FXML private TableColumn<UserDto, String> colCreated;

    @FXML private TableColumn<UserDto, String> colUpdated;

    @FXML private AnchorPane rootPane;


private UserBo userBo = (UserBo) BOFactory.getInstance().getBo(BOFactory.BOType.USER);
    private String currentUserId;

    private static String currentRole() {
        return DashboardController.userData == null ? "" : String.valueOf(DashboardController.userData.getRole());
    }

    private static boolean isAdmin() {
        return "admin".equals(currentRole());
    }

    private static boolean canUpdateUsers() {
        return isAdmin();
    }

    private static boolean canChangePassword() {
        return isAdmin();
    }

    private String visiblePassword(String password) {
        return canChangePassword() ? password : MASKED_PASSWORD;
    }

    private void setupShortcuts() {
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && cmbRole.isFocused()) {
                event.consume();
                submitForm();
                return;
            }

            if (!event.isControlDown() || event.isAltDown() || event.isMetaDown()) return;
            switch (event.getCode()) {
                case S -> {
                    event.consume();
                    submitForm();
                }
                case U -> {
                    event.consume();
                    if (!btnUpdate.isDisabled()) btnUpdate.fire();
                }
                case D -> {
                    event.consume();
                    if (!btnDelete.isDisabled()) btnDelete.fire();
                }
                case C -> {
                    event.consume();
                    btnClear.fire();
                }
                case F -> {
                    event.consume();
                    txtSearch.requestFocus();
                    txtSearch.selectAll();
                }
                default -> { }
            }
        });
    }

    private void submitForm() {
        if (cmbRole.getValue() == null) {
            txtName.requestFocus();
            return;
        }
        if (!btnSave.isDisabled()) btnSave.fire();
        else if (!btnUpdate.isDisabled()) btnUpdate.fire();
    }

    @FXML
    void onNameAction(ActionEvent event) {
        txtEmail.requestFocus();
    }

    @FXML
    void onEmailAction(ActionEvent event) {
        if (!txtPassword.isDisabled()) {
            txtPassword.requestFocus();
        } else {
            cmbRole.requestFocus();
        }
    }

    @FXML
    void onPasswordAction(ActionEvent event) {
        if (!txtPassword.isDisabled()) {
            cmbRole.requestFocus();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String cr = currentRole();
        if (isAdmin()) {
            cmbRole.getItems().addAll("admin", "manager", "casheer");
        } else if ("manager".equals(cr)) {
            cmbRole.getItems().addAll("casheer");
        } else {
            cmbRole.getItems().addAll("casheer");
        }
        
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(data -> new SimpleStringProperty(visiblePassword(data.getValue().getPassword())));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated.setCellFactory(AuditCell.factory());
        colUpdated.setCellFactory(AuditCell.factory());
        colCreated.setVisible(isAdmin());
        colUpdated.setVisible(isAdmin());
        tblUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        pageReset();
        loadAllUsers();
        generateNextId();

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        txtSearch.setTooltip(new Tooltip("Search User (Ctrl+F)"));
        setupShortcuts();

        tblUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentUserId = newValue.getId();
                txtName.setText(newValue.getName());
                txtEmail.setText(newValue.getEmail());
                cmbRole.getSelectionModel().select(RoleUtil.normalize(newValue.getRole()));
                loadPasswordForEditing(newValue.getPassword());

                btnSave.setDisable(true);
                btnUpdate.setDisable(!canUpdateUsers());
                btnDelete.setDisable(false);
            }
        });
    }

    private void loadPasswordForEditing(String password) {
        if (canChangePassword()) {
            txtPassword.clear();
            txtPassword.setPromptText("Password");
            txtPassword.setDisable(false);
            txtPassword.setText(password);
        } else {
            txtPassword.clear();
            txtPassword.setPromptText("Only admin can change");
            txtPassword.setDisable(true);
        }
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        txtPassword.clear();
        txtPassword.setPromptText("Password");
        txtPassword.setDisable(false);
    }

private void loadAllUsers() {
        tblUsers.getSelectionModel().clearSelection();
        ObservableList<UserDto> items = tblUsers.getItems();
        items.clear();
        ArrayList<UserDto> allUsers = userBo.getAllUsers();
        if (allUsers != null) items.addAll(allUsers);
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

        String cr = currentRole();
        if ("manager".equals(cr) && role != null && !"casheer".equals(role)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Managers can only create Casheer accounts!");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "All fields are required!");
            return;
        }

        if (!Validation.isValidEmail(email)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid email format!");
            return;
        }

        if (userBo.isEmailExist(email, null)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Email already exists!");
            return;
        }

        if (userBo.isNameExist(name, null)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Username already exists!");
            return;
        }

        if (password.length() < 5) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Password must be at least 5 characters!");
            return;
        }

        UserDto newUser = new UserDto(currentUserId, name, email, password, role);
        boolean success = userBo.saveUser(newUser);
        if (success) {
AlertUtil.showAlert(Alert.AlertType.INFORMATION, "User saved successfully!");
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save user!");
        }
    }

@FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (!canUpdateUsers()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can update users!");
            return;
        }

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String role = cmbRole.getValue();

        String cr = currentRole();
        if ("manager".equals(cr) && role != null && !"casheer".equals(role)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Managers can only update Casheer accounts!");
            return;
        }

        if (name.isEmpty() || email.isEmpty() || role == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "All fields are required!");
            return;
        }

        if (!canChangePassword() && !password.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can update passwords!");
            return;
        }

        if (!"U001".equals(currentUserId)) {
            if (!Validation.isValidEmail(email)) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid email format!");
                return;
            }
        }

        if (userBo.isEmailExist(email, currentUserId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Email already exists!");
            return;
        }

        if (userBo.isNameExist(name, currentUserId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Username already exists!");
            return;
        }

        if (!password.isEmpty() && password.length() < 5) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Password must be at least 5 characters!");
            return;
        }

        String finalPassword = password.isEmpty() ? existingPassword(currentUserId) : password;
        if (finalPassword == null || finalPassword.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to read the current password!");
            return;
        }

        UserDto updatedUser = new UserDto(currentUserId, name, email, finalPassword, role);
        boolean success = userBo.updateUser(updatedUser);
        if (success) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "User updated successfully!");
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update user!");
        }
    }

    private String existingPassword(String userId) {
        for (UserDto dto : userBo.getAllUsers()) {
            if (dto.getId().equals(userId)) {
                return dto.getPassword();
            }
        }
        return null;
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if ("U001".equals(currentUserId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cannot delete the main admin account!");
            return;
        }

        if (DashboardController.userData != null && DashboardController.userData.getId().equals(currentUserId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "You cannot delete your own account!");
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
AlertUtil.showAlert(Alert.AlertType.INFORMATION, "User deleted successfully!");
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete user!");
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

@FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            rootPane.requestFocus();
            loadAllUsers();
        } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
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
        List<UserDto> filtered = new ArrayList<>();

        for (UserDto dto : allUsers) {
            if (dto.getId().toLowerCase().contains(searchTerm) ||
                dto.getName().toLowerCase().contains(searchTerm) ||
                dto.getEmail().toLowerCase().contains(searchTerm)) {
                filtered.add(dto);
            }
        }

        tblUsers.getSelectionModel().clearSelection();
        ObservableList<UserDto> items = tblUsers.getItems();
        items.clear();
        items.addAll(filtered);
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
            cmbRole.getSelectionModel().select(found.getRole());
            loadPasswordForEditing(found.getPassword());
            tblUsers.getSelectionModel().select(found);
            tblUsers.scrollTo(found);

btnSave.setDisable(true);
            btnUpdate.setDisable(!canUpdateUsers());
            btnDelete.setDisable(false);
        } else {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "User not found!");
        }
    }

private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtPassword.clear();
        cmbRole.getSelectionModel().clearSelection();
        cmbRole.setValue(null);
        txtSearch.clear();
        tblUsers.getSelectionModel().clearSelection();

        pageReset();
        try {
            generateNextId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadAllUsers();
        txtName.requestFocus();
    }
}
