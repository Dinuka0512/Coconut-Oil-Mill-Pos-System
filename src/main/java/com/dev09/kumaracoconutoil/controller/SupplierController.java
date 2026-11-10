package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.SupplierBO;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditCell;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TableColumn<SupplierDto, String> colContact;
    @FXML private TableColumn<SupplierDto, String> colName;
    @FXML private TableColumn<SupplierDto, String> colCreated;
    @FXML private TableColumn<SupplierDto, String> colUpdated;
    @FXML private TableView<SupplierDto> tblSupplier;
    @FXML private TextField txtContact;
    @FXML private TextField txtName;
    @FXML private TextField txtSearch;
    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;

    private final SupplierBO supplierBO = (SupplierBO) BOFactory.getInstance().getBo(BOFactory.BOType.SUPPLIER);
    private String currentSupplierId;

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
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated.setCellFactory(AuditCell.factory());
        colUpdated.setCellFactory(AuditCell.factory());
        boolean canSeeAudit = DashboardController.userData != null && "admin".equals(DashboardController.userData.getRole());
        colCreated.setVisible(canSeeAudit);
        colUpdated.setVisible(canSeeAudit);
        tblSupplier.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        txtSearch.setTooltip(new Tooltip("Search Supplier (Ctrl+F)"));
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
        }

        loadAllSuppliers();
        generateNextId();
        setupShortcuts();

        tblSupplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentSupplierId = newValue.getSupplierId();
                txtName.setText(newValue.getName());
                txtContact.setText(newValue.getContact());

                btnSave.setDisable(true);
                boolean isCashier = DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole());
                btnUpdate.setDisable(isCashier);
                btnDelete.setDisable(isCashier);
            }
        });
    }

    private void loadAllSuppliers() {
        tblSupplier.getSelectionModel().clearSelection();
        ObservableList<SupplierDto> items = tblSupplier.getItems();
        items.clear();
        ArrayList<SupplierDto> allSuppliers = supplierBO.getAllSuppliers();
        if (allSuppliers != null) items.addAll(allSuppliers);
    }

    private void generateNextId() {
        currentSupplierId = supplierBO.generateNewSupplierId();
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentSupplierId;
        String name = txtName.getText();
        String contact = txtContact.getText();

        if (name == null || name.trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Name Required!");
            return;
        }

        boolean isSaved = supplierBO.saveSupplier(new SupplierDto(id, name, contact));
        if (isSaved) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Supplier Saved!");
            loadAllSuppliers();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save supplier!");
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        String id = currentSupplierId;
        String name = txtName.getText();
        String contact = txtContact.getText();

        boolean isUpdated = supplierBO.updateSupplier(new SupplierDto(id, name, contact));
        if (isUpdated) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Supplier Updated!");
            loadAllSuppliers();
            clearFields();
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update supplier!");
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Supplier");
        confirmAlert.setHeaderText("Are you sure you want to delete this supplier?");
        confirmAlert.setContentText("Supplier: " + txtName.getText() + " (" + currentSupplierId + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String id = currentSupplierId;
            boolean isDeleted = supplierBO.deleteSupplier(id);
            if (isDeleted) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Supplier Deleted!");
                loadAllSuppliers();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete supplier!");
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void onNameAction(ActionEvent event) {
        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Name Required!");
            txtName.requestFocus();
            return;
        }
        txtContact.requestFocus();
    }

    @FXML
    void onContactAction(ActionEvent event) {
        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) {
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
        tblSupplier.getSelectionModel().clearSelection();
        txtName.requestFocus();
    }

    private void pageReset() {
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            if (txtSearch.getParent() != null) txtSearch.getParent().requestFocus();
            loadAllSuppliers();
        } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            performSearchWithAlert();
        } else {
            performSearch();
        }
    }

    private void performSearch() {
        String searchTerm = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllSuppliers();
            return;
        }

        ArrayList<SupplierDto> allSuppliers = supplierBO.getAllSuppliers();
        tblSupplier.getSelectionModel().clearSelection();
        ObservableList<SupplierDto> items = tblSupplier.getItems();
        items.clear();
        if (allSuppliers == null) return;
        for (SupplierDto dto : allSuppliers) {
            if ((dto.getSupplierId() != null && dto.getSupplierId().toLowerCase().contains(searchTerm)) ||
                    (dto.getName() != null && dto.getName().toLowerCase().contains(searchTerm)) ||
                    (dto.getContact() != null && dto.getContact().toLowerCase().contains(searchTerm))) {
                items.add(dto);
            }
        }
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllSuppliers();
            return;
        }

        performSearch();
        if (tblSupplier.getItems().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Supplier not found!");
        } else {
            SupplierDto foundSupplier = tblSupplier.getItems().get(0);
            currentSupplierId = foundSupplier.getSupplierId();
            txtName.setText(foundSupplier.getName());
            txtContact.setText(foundSupplier.getContact());
            tblSupplier.getSelectionModel().select(foundSupplier);
            tblSupplier.scrollTo(foundSupplier);
            btnSave.setDisable(true);
            boolean isCashier = DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole());
            btnUpdate.setDisable(isCashier);
            btnDelete.setDisable(isCashier);
        }
    }

    @FXML
    void exportSupplier(ActionEvent event) throws IOException {
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
            var suppliers = supplierBO.getAllSuppliersEntity();
            byte[] bytes;
            String fileName;
            if (result.get() == btn80mm) {
                bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.SupplierReportThermal(suppliers);
                fileName = "SupplierReport_80mm";
            } else if (result.get() == btnA4) {
                bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.SupplierReportA4Pro(suppliers);
                fileName = "SupplierReport_A4";
            } else {
                return;
            }
            File tempFile = File.createTempFile(fileName, ".pdf");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), bytes);
            Desktop.getDesktop().open(tempFile);
        }
    }
}
