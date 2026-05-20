package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.File;
import java.nio.file.Files;
import java.awt.Desktop;
import javafx.scene.control.ChoiceDialog;

public class ProductController implements Initializable {

    @FXML
    private TableColumn<ProductDto, String> colItemCode;

    @FXML
    private TableColumn<ProductDto, String> colName;

    @FXML
    private TableColumn<ProductDto, Double> colPrice;

    @FXML
    private TableColumn<ProductDto, Double> colQty;

    @FXML
    private TableView<ProductDto> tblProduct;

    @FXML
    private TextField txtItemCode;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtPrice;

    @FXML
    private TextField txtQty;

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

    private ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private String currentProductId;

    private void setupShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> btnSave.fire());
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.U, KeyCombination.CONTROL_DOWN),
                () -> btnUpdate.fire());
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.D, KeyCombination.CONTROL_DOWN),
                () -> btnDelete.fire());
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.F, KeyCombination.CONTROL_DOWN),
                () -> { txtSearch.requestFocus(); txtSearch.selectAll(); });
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.C, KeyCombination.CONTROL_DOWN),
                () -> btnClear.fire());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageReset();
        colItemCode.setCellValueFactory(new PropertyValueFactory<>("itemcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));

        loadAllProducts();
        generateNextId();

        txtItemCode.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupShortcuts(newScene);
            }
        });

        tblProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentProductId = newValue.getId();
                txtItemCode.setText(newValue.getItemcode());
                txtName.setText(newValue.getName());
                txtPrice.setText(String.valueOf(newValue.getPrice()));
                txtQty.setText(String.valueOf(newValue.getQty()));

                if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
                    btnDelete.setDisable(true);
                    btnUpdate.setDisable(true);
                } else {
                    btnDelete.setDisable(false);
                    btnUpdate.setDisable(false);
                }
                btnSave.setDisable(true);
            }
        });
    }

    @FXML
    void exportAllItems(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            new Alert(Alert.AlertType.WARNING, "You don't have permission to export reports!").show();
            return;
        }
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
            try {
                var products = productBO.getAllProductsEntity();
                byte[] bytes;
                String fileName;
                if (result.get() == btn80mm) {
                    bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.ProductReportThermal(products);
                    fileName = "ProductReport_80mm";
                } else if (result.get() == btnA4) {
                    bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.ProductReportA4Pro(products);
                    fileName = "ProductReport_A4";
                } else {
                    return;
                }
                File tempFile = File.createTempFile(fileName, ".pdf");
                Files.write(tempFile.toPath(), bytes);
                Desktop.getDesktop().open(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error generating report: " + e.getMessage()).show();
            }
        }
    }

    private void pageReset() {
        btnDelete.setDisable(true);
        btnUpdate.setDisable(true);
        btnSave.setDisable(false);
    }

    private void loadAllProducts() {
        tblProduct.getItems().clear();
        ArrayList<ProductDto> allProducts = productBO.getAllProducts();
        tblProduct.setItems(FXCollections.observableArrayList(allProducts));
    }

    private void generateNextId() {
        currentProductId = productBO.generateNewProductId();
        txtName.setText("");
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentProductId;
        String itemcode = txtItemCode.getText();
        String name = txtName.getText();
        String priceText = txtPrice.getText();
        String qtyText = txtQty.getText();

        if (itemcode.isEmpty() || name.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Mandatory fields are empty!").show();
            return;
        }

        if (productBO.existProductByItemCode(itemcode)) {
            new Alert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.").show();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            double qty = Double.parseDouble(qtyText);

            boolean isSaved = productBO.saveProduct(new ProductDto(id, itemcode, name, qty, price));
            if (isSaved) {
                new Alert(Alert.AlertType.INFORMATION, "Product Saved!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save inventory!").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input for price or quantity!").show();
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            new Alert(Alert.AlertType.WARNING, "Only admins can update inventory!").show();
            return;
        }
        String id = currentProductId;
        String itemcode = txtItemCode.getText();
        String name = txtName.getText();
        String priceText = txtPrice.getText();
        String qtyText = txtQty.getText();

        if (itemcode.isEmpty() || name.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Mandatory fields are empty!").show();
            return;
        }

        ProductDto currentProduct = productBO.searchProduct(id);
        if (currentProduct != null && !itemcode.equals(currentProduct.getItemcode()) && productBO.existProductByItemCode(itemcode)) {
            new Alert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.").show();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            double qty = Double.parseDouble(qtyText);

            boolean isUpdated = productBO.updateProduct(new ProductDto(id, itemcode, name, qty, price));
            if (isUpdated) {
                new Alert(Alert.AlertType.INFORMATION, "Product Updated!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update inventory!").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input for price or quantity!").show();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            new Alert(Alert.AlertType.WARNING, "Only admins can delete inventory!").show();
            return;
        }
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Are you sure you want to delete this inventory item?");
        confirmAlert.setContentText("Product: " + txtName.getText() + " (" + currentProductId + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isDeleted = productBO.deleteProduct(currentProductId);
            if (isDeleted) {
                new Alert(Alert.AlertType.INFORMATION, "Product Deleted!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete inventory!").show();
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    @FXML
    void onItemCodeAction(ActionEvent event) {
        txtName.requestFocus();
    }

    @FXML
    void onNameAction(ActionEvent event) {
        txtPrice.requestFocus();
    }

    @FXML
    void onPriceAction(ActionEvent event) {
        txtQty.requestFocus();
    }

    @FXML
    void onQtyAction(ActionEvent event) {
        if (!btnSave.isDisabled()) {
            btnSave.fire();
        } else if (!btnUpdate.isDisabled()) {
            btnUpdate.fire();
        }
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtItemCode.clear();
        txtName.clear();
        txtPrice.clear();
        txtQty.clear();
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
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        ObservableList<ProductDto> allProducts = tblProduct.getItems();
        ProductDto foundProduct = null;

        for (ProductDto dto : allProducts) {
            String itemcode = dto.getItemcode();
            if (dto.getId().toLowerCase().equals(searchTerm) ||
                    (itemcode != null && itemcode.toLowerCase().contains(searchTerm)) ||
                    dto.getName().toLowerCase().contains(searchTerm)) {
                foundProduct = dto;
                break;
            }
        }

        if (foundProduct != null) {
            currentProductId = foundProduct.getId();
            txtItemCode.setText(foundProduct.getItemcode());
            txtName.setText(foundProduct.getName());
            txtPrice.setText(String.valueOf(foundProduct.getPrice()));
            txtQty.setText(String.valueOf(foundProduct.getQty()));
            tblProduct.getSelectionModel().select(foundProduct);
            tblProduct.scrollTo(foundProduct);
        }
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        ObservableList<ProductDto> allProducts = tblProduct.getItems();
        ProductDto foundProduct = null;

        for (ProductDto dto : allProducts) {
            String itemcode = dto.getItemcode();
            if (dto.getId().toLowerCase().equals(searchTerm) ||
                    (itemcode != null && itemcode.toLowerCase().contains(searchTerm)) ||
                    dto.getName().toLowerCase().contains(searchTerm)) {
                foundProduct = dto;
                break;
            }
        }

        if (foundProduct != null) {
            currentProductId = foundProduct.getId();
            txtItemCode.setText(foundProduct.getItemcode());
            txtName.setText(foundProduct.getName());
            txtPrice.setText(String.valueOf(foundProduct.getPrice()));
            txtQty.setText(String.valueOf(foundProduct.getQty()));
            tblProduct.getSelectionModel().select(foundProduct);
            tblProduct.scrollTo(foundProduct);
        } else {
            new Alert(Alert.AlertType.WARNING, "Inventory not found!").show();
        }
    }
}