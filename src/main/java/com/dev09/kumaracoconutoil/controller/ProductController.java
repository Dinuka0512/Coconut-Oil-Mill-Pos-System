package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;

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
                () -> {
                    if (txtName.getScene() != scene) return;
                    btnSave.fire();
                });
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.U, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (txtName.getScene() != scene) return;
                    btnUpdate.fire();
                });
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.D, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (txtName.getScene() != scene) return;
                    btnDelete.fire();
                });
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.F, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (txtName.getScene() != scene) return;
                    txtSearch.requestFocus();
                    txtSearch.selectAll();
                });
        scene.getAccelerators().put(
                new KeyCodeCombination(javafx.scene.input.KeyCode.C, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (txtName.getScene() != scene) return;
                    btnClear.fire();
                });
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
            AlertUtil.showAlert(Alert.AlertType.WARNING, "You don't have permission to export reports!");
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
                tempFile.deleteOnExit();
                Files.write(tempFile.toPath(), bytes);
                Desktop.getDesktop().open(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error generating report: " + e.getMessage());
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
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Mandatory fields are empty!");
            return;
        }

        if (productBO.existProductByItemCode(itemcode)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            double qty = Double.parseDouble(qtyText);

            boolean isSaved = productBO.saveProduct(new ProductDto(id, itemcode, name, qty, price));
            if (isSaved) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Saved!");
                loadAllProducts();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save inventory!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price or quantity!");
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can update inventory!");
            return;
        }
        String id = currentProductId;
        String itemcode = txtItemCode.getText();
        String name = txtName.getText();
        String priceText = txtPrice.getText();
        String qtyText = txtQty.getText();

        if (itemcode.isEmpty() || name.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Mandatory fields are empty!");
            return;
        }

        ProductDto currentProduct = productBO.searchProduct(id);
        if (currentProduct != null && !itemcode.equals(currentProduct.getItemcode()) && productBO.existProductByItemCode(itemcode)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            double qty = Double.parseDouble(qtyText);

            boolean isUpdated = productBO.updateProduct(new ProductDto(id, itemcode, name, qty, price));
            if (isUpdated) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
                loadAllProducts();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price or quantity!");
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can delete inventory!");
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
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Deleted!");
                loadAllProducts();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete inventory!");
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
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            txtSearch.getParent().requestFocus();
        } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            performSearchWithAlert();
        } else {
            performSearch();
        }
    }

    private ProductDto findProductBySearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return null;
        ObservableList<ProductDto> allProducts = tblProduct.getItems();
        for (ProductDto dto : allProducts) {
            String itemcode = dto.getItemcode();
            if (dto.getId().toLowerCase().equals(searchTerm) ||
                    (itemcode != null && itemcode.toLowerCase().contains(searchTerm)) ||
                    dto.getName().toLowerCase().contains(searchTerm)) {
                return dto;
            }
        }
        return null;
    }

    private void selectProduct(ProductDto foundProduct) {
        if (foundProduct == null) return;
        currentProductId = foundProduct.getId();
        txtItemCode.setText(foundProduct.getItemcode());
        txtName.setText(foundProduct.getName());
        txtPrice.setText(String.valueOf(foundProduct.getPrice()));
        txtQty.setText(String.valueOf(foundProduct.getQty()));
        tblProduct.getSelectionModel().select(foundProduct);
        tblProduct.scrollTo(foundProduct);
    }

    private void performSearch() {
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }
        selectProduct(findProductBySearchTerm(searchTerm));
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }
        ProductDto found = findProductBySearchTerm(searchTerm);
        if (found != null) {
            selectProduct(found);
        } else {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Inventory not found!");
        }
    }
}