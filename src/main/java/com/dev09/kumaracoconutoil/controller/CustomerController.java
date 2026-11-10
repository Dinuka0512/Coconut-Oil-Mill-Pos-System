package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditCell;
import com.dev09.kumaracoconutoil.util.RoleUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import java.awt.Desktop;

public class CustomerController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TableColumn<CustomerDto, String> colContact;

    @FXML
    private TableColumn<CustomerDto, String> colName;

    @FXML
    private TableColumn<CustomerDto, Double> colDue;

    @FXML
    private TableColumn<CustomerDto, String> colCreated;

    @FXML
    private TableColumn<CustomerDto, String> colUpdated;

    @FXML
    private TableView<CustomerDto> tblCustomer;

    @FXML
    private TextField txtContact;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtDue;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnUpdate;

    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private String currentCustomerId;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageReset();

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueAmount"));

        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated.setCellFactory(AuditCell.factory());
        colUpdated.setCellFactory(AuditCell.factory());
        boolean canSeeAudit = DashboardController.userData != null && "admin".equals(DashboardController.userData.getRole());
        colCreated.setVisible(canSeeAudit);
        colUpdated.setVisible(canSeeAudit);
        tblCustomer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        txtSearch.setTooltip(new Tooltip("Search Customer (Ctrl+F)"));
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
        }

        loadAllCustomers();
        generateNextId();

        setupShortcuts();
        javafx.application.Platform.runLater(() -> txtName.requestFocus());

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentCustomerId = newValue.getCustId();
                txtName.setText(newValue.getName());
                txtContact.setText(newValue.getContact());
                double due = newValue.getDueAmount();
                txtDue.setText(due == 0 ? "0" : String.format("%.2f", due));

                btnSave.setDisable(true);
                boolean isClient = DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole());
                btnUpdate.setDisable(isClient);
                btnDelete.setDisable(isClient);
            }
        });
    }

    //GET THE ALL CUSTOMERS ....
    private void loadAllCustomers() {
        tblCustomer.getSelectionModel().clearSelection();
        ObservableList<CustomerDto> items = tblCustomer.getItems();
        items.clear();
        ArrayList<CustomerDto> allCustomers = customerBO.getAllCustomers();
        if (allCustomers != null) items.addAll(allCustomers);
    }

    //GET THE NEXT CUSTOMER ID
    private void generateNextId() {
        currentCustomerId = customerBO.generateNewCustomerId();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentCustomerId;
        String name = txtName.getText();
        String contact = txtContact.getText();

        if (name.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Name Required!");
            return;
        }

        boolean isSaved = customerBO.saveCustomer(new CustomerDto(id, name, contact));
        if (isSaved) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Customer Saved!");
            loadAllCustomers();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save customer!");
        }
    }

    //CUSTOMER UPDATE HERE
    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (AppConstants.DEMO_CUSTOMER_ID.equals(currentCustomerId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer cannot be updated!");
            return;
        }

        String id = currentCustomerId;
        String name = txtName.getText();
        String contact = txtContact.getText();

        boolean isUpdated = customerBO.updateCustomer(new CustomerDto(id, name, contact));
        if (isUpdated) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Customer Updated!");
            loadAllCustomers();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update customer!");
        }
    }

    //CUSTOMER DELETE HERE (WITH CONFIRMATION)
    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (AppConstants.DEMO_CUSTOMER_ID.equals(currentCustomerId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer cannot be deleted!");
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Customer");
        confirmAlert.setHeaderText("Are you sure you want to delete this customer?");
        confirmAlert.setContentText("Customer: " + txtName.getText() + " (" + currentCustomerId + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String id = currentCustomerId;
            boolean isDeleted = customerBO.deleteCustomer(id);
            if (isDeleted) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Customer Deleted!");
                loadAllCustomers();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete customer!");
            }
        }
    }

    //CLEAR THE TEXT INPUTS
    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void onNameAction(ActionEvent event) {
        if (txtName.getText().trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Name Required!");
            txtName.requestFocus();
            return;
        }
        txtContact.requestFocus();
    }

    @FXML
    void onContactAction(ActionEvent event) {
        if (txtName.getText().trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Name Required!");
            txtName.requestFocus();
            return;
        }
        if (!btnSave.isDisabled()) {
            btnSave.fire();
        } else if (!btnUpdate.isDisabled()) {
            btnUpdate.fire();
        }
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtName.clear();
        txtContact.clear();
        txtDue.clear();
        txtName.requestFocus();
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    //CUSTOMER SEARCH
    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            txtSearch.getParent().requestFocus();
        } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            performSearchWithAlert();
        } else {
            performSearch();
        }
    }

    private void performSearch() {
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllCustomers();
            return;
        }

        ObservableList<CustomerDto> allCustomers = tblCustomer.getItems();
        CustomerDto foundCustomer = null;

        for (CustomerDto dto : allCustomers) {
            if (dto.getCustId().toLowerCase().equals(searchTerm) ||
                    dto.getName().toLowerCase().contains(searchTerm) ||
                    dto.getContact().toLowerCase().contains(searchTerm)) {
                foundCustomer = dto;
                break;
            }
        }

        if (foundCustomer != null) {
            currentCustomerId = foundCustomer.getCustId();
            txtName.setText(foundCustomer.getName());
            txtContact.setText(foundCustomer.getContact());
            double due1 = foundCustomer.getDueAmount();
            txtDue.setText(due1 == 0 ? "0" : String.format("%.2f", due1));
            tblCustomer.getSelectionModel().select(foundCustomer);
            tblCustomer.scrollTo(foundCustomer);
        }
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllCustomers();
            return;
        }

        ObservableList<CustomerDto> allCustomers = tblCustomer.getItems();
        CustomerDto foundCustomer = null;

        for (CustomerDto dto : allCustomers) {
            if (dto.getCustId().toLowerCase().equals(searchTerm) ||
                    dto.getName().toLowerCase().contains(searchTerm) ||
                    dto.getContact().toLowerCase().contains(searchTerm)) {
                foundCustomer = dto;
                break;
            }
        }

        if (foundCustomer != null) {
            currentCustomerId = foundCustomer.getCustId();
            txtName.setText(foundCustomer.getName());
            txtContact.setText(foundCustomer.getContact());
            double due2 = foundCustomer.getDueAmount();
            txtDue.setText(due2 == 0 ? "0" : String.format("%.2f", due2));
            tblCustomer.getSelectionModel().select(foundCustomer);
            tblCustomer.scrollTo(foundCustomer);
        } else {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Customer not found!");
        }
    }

    @FXML
    void exportCustomer(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Select Format");
        alert.setHeaderText("How do you need to print?");
        alert.setContentText("Choose an option:");

        ButtonType btn80mm = new ButtonType("Small (80mm)");
        ButtonType btnA4 = new ButtonType("A4 Page");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btn80mm, btnA4, btnCancel);

        var result = alert.showAndWait();
        if (result.isPresent()) {
            var customers = customerBO.getAllCustomersEntity();
            byte[] bytes;
            String fileName;
            if (result.get() == btn80mm) {
                bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.CustomerReportThermal(customers);
                fileName = "CustomerReport_80mm";
            } else if (result.get() == btnA4) {
                bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.CustomerReportA4Pro(customers);
                fileName = "CustomerReport_A4";
            } else {
                return;
            }
            File tempFile = File.createTempFile(fileName, ".pdf");
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), bytes);
            java.awt.Desktop.getDesktop().open(tempFile);
        }
    }
}