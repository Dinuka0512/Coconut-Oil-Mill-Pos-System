package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;

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
    private TableColumn<CustomerDto, String> colContact;

    @FXML
    private TableColumn<CustomerDto, String> colName;

    @FXML
    private TableView<CustomerDto> tblCustomer;

    @FXML
    private TextField txtContact;

    @FXML
    private TextField txtName;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageReset();

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));

        loadAllCustomers();
        generateNextId();

        tblCustomer.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentCustomerId = newValue.getCustId();
                txtName.setText(newValue.getName());
                txtContact.setText(newValue.getContact());

                // Disable Save, Enable Update/Delete
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });
    }

    //GET THE ALL CUSTOMERS ....
    private void loadAllCustomers() {
        tblCustomer.getItems().clear();
        ArrayList<CustomerDto> allCustomers = customerBO.getAllCustomers();
        tblCustomer.setItems(FXCollections.observableArrayList(allCustomers));
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
            new Alert(Alert.AlertType.WARNING, "Name Required!").show();
            return;
        }

        boolean isSaved = customerBO.saveCustomer(new CustomerDto(id, name, contact));
        if (isSaved) {
            new Alert(Alert.AlertType.INFORMATION, "Customer Saved!").show();
            loadAllCustomers();
            clearFields();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save customer!").show();
        }
    }

    //CUSTOMER UPDATE HERE
    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        String id = currentCustomerId;
        String name = txtName.getText();
        String contact = txtContact.getText();

        boolean isUpdated = customerBO.updateCustomer(new CustomerDto(id, name, contact));
        if (isUpdated) {
            new Alert(Alert.AlertType.INFORMATION, "Customer Updated!").show();
            loadAllCustomers();
            clearFields();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update customer!").show();
        }
    }

    //CUSTOMER DELETE HERE (WITH CONFIRMATION)
    @FXML
    void btnDeleteOnAction(ActionEvent event) {
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
                new Alert(Alert.AlertType.INFORMATION, "Customer Deleted!").show();
                loadAllCustomers();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete customer!").show();
            }
        }
    }

    //CLEAR THE TEXT INPUTS
    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtName.clear();
        txtContact.clear();
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    //CUSTOMER SEARCH
    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
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
            tblCustomer.getSelectionModel().select(foundCustomer);
            tblCustomer.scrollTo(foundCustomer);
        } else {
            new Alert(Alert.AlertType.WARNING, "Customer not found!").show();
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
            java.nio.file.Files.write(tempFile.toPath(), bytes);
            java.awt.Desktop.getDesktop().open(tempFile);
        }
    }
}