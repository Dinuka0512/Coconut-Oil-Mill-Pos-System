package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.SupplierBO;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AppConstants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Modal used to pick a supplier. The dropdown lists the saved suppliers and
 * the Name / Contact labels update as soon as a supplier is chosen.
 */
public class SupplierSelectionController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ComboBox<SupplierDto> cmbSupplier;
    @FXML
    private Label lblName;
    @FXML
    private Label lblContact;
    @FXML
    private Button btnSelect;
    @FXML
    private Button btnCancel;

    private final SupplierBO supplierBO = (SupplierBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.SUPPLIER);

    private SupplierDto selectedSupplier;
    private boolean confirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbSupplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(SupplierDto supplier) {
                if (supplier == null) return "";
                return supplier.getName() == null ? "-" : supplier.getName();
            }

            @Override
            public SupplierDto fromString(String text) {
                return null;
            }
        });

        loadSuppliers();

        cmbSupplier.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateLabels(newValue));
        updateLabels(cmbSupplier.getSelectionModel().getSelectedItem());

        // Enter confirms the highlighted supplier; Esc closes the dropdown first.
        cmbSupplier.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                if (cmbSupplier.isShowing()) {
                    cmbSupplier.hide();
                }
                confirm();
            } else if (event.getCode() == KeyCode.ESCAPE && !cmbSupplier.isShowing()) {
                event.consume();
                closeStage();
            }
        });

        btnSelect.setDefaultButton(true);

        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                rootPane.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.ESCAPE), this::closeStage);
            }
            cmbSupplier.requestFocus();
        });
    }

    private void loadSuppliers() {
        try {
            // Make sure the built-in supplier exists even if the app was already
            // running before this default was added; restores soft-deleted rows.
            supplierBO.ensureDefaultSupplier(
                    AppConstants.DEFAULT_SUPPLIER_ID,
                    AppConstants.DEFAULT_SUPPLIER_NAME,
                    AppConstants.DEFAULT_SUPPLIER_CONTACT);

            ArrayList<SupplierDto> suppliers = supplierBO.getAllSuppliers();
            cmbSupplier.setItems(FXCollections.observableArrayList(
                    suppliers == null ? new ArrayList<>() : suppliers));

            // Preselect the default supplier, mirroring the Demo customer in POS.
            for (int i = 0; i < cmbSupplier.getItems().size(); i++) {
                if (AppConstants.DEFAULT_SUPPLIER_ID.equals(cmbSupplier.getItems().get(i).getSupplierId())) {
                    cmbSupplier.getSelectionModel().select(i);
                    return;
                }
            }
            if (!cmbSupplier.getItems().isEmpty()) {
                cmbSupplier.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Could not load suppliers!");
        }
    }

    private void updateLabels(SupplierDto supplier) {
        lblName.setText("Name : " + (supplier == null || supplier.getName() == null ? "-" : supplier.getName()));
        lblContact.setText("Contact : " + (supplier == null || supplier.getContact() == null ? "-" : supplier.getContact()));
    }

    public SupplierDto getSelectedSupplier() {
        return selectedSupplier;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    void btnSelectOnAction(ActionEvent event) {
        confirm();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStage();
    }

    private void confirm() {
        SupplierDto supplier = cmbSupplier.getSelectionModel().getSelectedItem();
        if (supplier == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a supplier first!");
            cmbSupplier.requestFocus();
            return;
        }
        confirmed = true;
        selectedSupplier = supplier;
        closeStage();
    }

    private void closeStage() {
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
