package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.BankAccountBO;
import com.dev09.kumaracoconutoil.dto.BankAccountDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import com.dev09.kumaracoconutoil.util.UserLogUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/** Admin-only screen for maintaining the company's bank accounts. */
public class BankAccountsController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TextField txtBankName;
    @FXML private TextField txtAccountNumber;
    @FXML private TextField txtHolderName;
    @FXML private TextField txtBranch;
    @FXML private TextField txtSearch;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;
    @FXML private TableView<BankAccountDto> tblBankAccounts;
    @FXML private TableColumn<BankAccountDto, String> colBankName;
    @FXML private TableColumn<BankAccountDto, String> colAccountNumber;
    @FXML private TableColumn<BankAccountDto, String> colHolderName;
    @FXML private TableColumn<BankAccountDto, String> colBranch;

    private final BankAccountBO bankAccountBO = (BankAccountBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.BANK_ACCOUNT);
    private String currentBankId;

    private static String currentRole() {
        return DashboardController.userData == null ? "" : String.valueOf(DashboardController.userData.getRole());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colBankName.setCellValueFactory(new PropertyValueFactory<>("bankName"));
        colAccountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colHolderName.setCellValueFactory(new PropertyValueFactory<>("holderName"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branch"));
        tblBankAccounts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblBankAccounts.setPlaceholder(new Label("No bank accounts added yet"));

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        txtSearch.setTooltip(new Tooltip("Search (Ctrl+F)"));

        installEscapeHandler();

        if (!RoleUtil.isAdmin(currentRole())) {
            setFormDisabled(true);
            javafx.application.Platform.runLater(() -> AlertUtil.showAlert(
                    Alert.AlertType.WARNING, "Only admins can manage bank accounts."));
            return;
        }

        loadAllBankAccounts();
        generateNextId();
        setupShortcuts();
        javafx.application.Platform.runLater(() -> txtBankName.requestFocus());

        tblBankAccounts.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            currentBankId = newValue.getBankId();
            txtBankName.setText(newValue.getBankName());
            txtAccountNumber.setText(newValue.getAccountNumber());
            txtHolderName.setText(newValue.getHolderName());
            txtBranch.setText(newValue.getBranch());
            btnSave.setDisable(true);
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
        });
    }

    /** Same behaviour as the POS screen: Esc closes, asking first if data was entered. */
    private void installEscapeHandler() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.ESCAPE),
                        this::handleEscape
                );
            }
        });
    }

    private void handleEscape() {
        // The search field's own Esc handler clears the filter first.
        if (txtSearch != null && txtSearch.isFocused() && !isBlank(txtSearch.getText())) {
            return;
        }
        if (hasEnteredData() && !AlertUtil.confirmClose()) {
            return;
        }
        closeStage();
    }

    private boolean hasEnteredData() {
        return !isBlank(txtBankName.getText()) || !isBlank(txtAccountNumber.getText())
                || !isBlank(txtHolderName.getText()) || !isBlank(txtBranch.getText())
                || !isBlank(txtSearch.getText());
    }

    private void closeStage() {
        if (rootPane.getScene() == null || rootPane.getScene().getWindow() == null) return;
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    private void setFormDisabled(boolean disabled) {
        txtBankName.setDisable(disabled);
        txtAccountNumber.setDisable(disabled);
        txtHolderName.setDisable(disabled);
        txtBranch.setDisable(disabled);
        txtSearch.setDisable(disabled);
        btnSave.setDisable(disabled);
        btnUpdate.setDisable(disabled);
        btnDelete.setDisable(disabled);
        btnClear.setDisable(disabled);
        tblBankAccounts.setDisable(disabled);
    }

    private void setupShortcuts() {
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isControlDown() || event.isAltDown() || event.isMetaDown()) return;
            switch (event.getCode()) {
                case S -> {
                    event.consume();
                    if (!btnSave.isDisabled()) btnSave.fire();
                    else if (!btnUpdate.isDisabled()) btnUpdate.fire();
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

    private void loadAllBankAccounts() {
        tblBankAccounts.getSelectionModel().clearSelection();
        ObservableList<BankAccountDto> items = tblBankAccounts.getItems();
        items.clear();
        ArrayList<BankAccountDto> all = bankAccountBO.getAllBankAccounts();
        if (all != null) items.addAll(all);
    }

    private void generateNextId() {
        currentBankId = bankAccountBO.generateNewBankAccountId();
    }

    private boolean validateFields() {
        if (isBlank(txtBankName.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Bank Name Required!");
            txtBankName.requestFocus();
            return false;
        }
        if (isBlank(txtAccountNumber.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Account Number Required!");
            txtAccountNumber.requestFocus();
            return false;
        }
        if (isBlank(txtHolderName.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Account Holder Name Required!");
            txtHolderName.requestFocus();
            return false;
        }
        if (isBlank(txtBranch.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Branch Required!");
            txtBranch.requestFocus();
            return false;
        }
        return true;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!validateFields()) return;
        BankAccountDto dto = new BankAccountDto(
                currentBankId,
                txtBankName.getText().trim(),
                txtAccountNumber.getText().trim(),
                txtHolderName.getText().trim(),
                txtBranch.getText().trim()
        );
        if (bankAccountBO.saveBankAccount(dto)) {
            UserLogUtil.record("BANK_ACCOUNT_ADDED " + dto.getBankName());
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Bank Account Saved!");
            loadAllBankAccounts();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save bank account!");
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (currentBankId == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a bank account to update!");
            return;
        }
        if (!validateFields()) return;
        BankAccountDto dto = new BankAccountDto(
                currentBankId,
                txtBankName.getText().trim(),
                txtAccountNumber.getText().trim(),
                txtHolderName.getText().trim(),
                txtBranch.getText().trim()
        );
        if (bankAccountBO.updateBankAccount(dto)) {
            UserLogUtil.record("BANK_ACCOUNT_UPDATED " + dto.getBankName());
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Bank Account Updated!");
            loadAllBankAccounts();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update bank account!");
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (currentBankId == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a bank account to delete!");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Bank Account");
        confirmAlert.setHeaderText("Are you sure you want to delete this bank account?");
        confirmAlert.setContentText(txtBankName.getText() + " - " + txtAccountNumber.getText());
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (bankAccountBO.deleteBankAccount(currentBankId)) {
            UserLogUtil.record("BANK_ACCOUNT_DELETED " + txtBankName.getText());
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Bank Account Deleted!");
            loadAllBankAccounts();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete bank account!");
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void onBankNameAction(ActionEvent event) {
        if (isBlank(txtBankName.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Bank Name Required!");
            txtBankName.requestFocus();
            return;
        }
        txtAccountNumber.requestFocus();
    }

    @FXML
    void onAccountNumberAction(ActionEvent event) {
        if (isBlank(txtAccountNumber.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Account Number Required!");
            txtAccountNumber.requestFocus();
            return;
        }
        txtHolderName.requestFocus();
    }

    @FXML
    void onHolderNameAction(ActionEvent event) {
        if (isBlank(txtHolderName.getText())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Account Holder Name Required!");
            txtHolderName.requestFocus();
            return;
        }
        txtBranch.requestFocus();
    }

    @FXML
    void onBranchAction(ActionEvent event) {
        if (!validateFields()) return;
        if (!btnSave.isDisabled()) btnSave.fire();
        else if (!btnUpdate.isDisabled()) btnUpdate.fire();
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            loadAllBankAccounts();
        } else {
            performSearch();
        }
    }

    private void performSearch() {
        String term = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        ObservableList<BankAccountDto> items = tblBankAccounts.getItems();
        items.clear();
        if (term.isEmpty()) {
            ArrayList<BankAccountDto> all = bankAccountBO.getAllBankAccounts();
            if (all != null) items.addAll(all);
            return;
        }
        ArrayList<BankAccountDto> all = bankAccountBO.getAllBankAccounts();
        if (all == null) return;
        for (BankAccountDto dto : all) {
            if (contains(dto.getBankId(), term) || contains(dto.getBankName(), term)
                    || contains(dto.getAccountNumber(), term) || contains(dto.getHolderName(), term)
                    || contains(dto.getBranch(), term)) {
                items.add(dto);
            }
        }
    }

    private static boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtBankName.clear();
        txtAccountNumber.clear();
        txtHolderName.clear();
        txtBranch.clear();
        txtSearch.clear();
        loadAllBankAccounts();
        txtBankName.requestFocus();
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }
}
