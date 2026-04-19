package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    @FXML
    private TableColumn<ProductDto, String> colName;

    @FXML
    private TableColumn<ProductDto, Double> colPrice;

    @FXML
    private TableColumn<ProductDto, Integer> colQty;

    @FXML
    private TableView<ProductDto> tblProduct;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageReset();
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

        loadAllProducts();
        generateNextId();

        tblProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentProductId = newValue.getId();
                txtName.setText(newValue.getName());
                txtPrice.setText(String.valueOf(newValue.getPrice()));
                txtQty.setText(String.valueOf(newValue.getQty()));

                btnDelete.setDisable(false);
                btnUpdate.setDisable(false);
                btnSave.setDisable(true);
            }
        });
    }

    @FXML
    void exportAllItems(ActionEvent event) {
        // TODO: implement PDF export if needed
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
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentProductId;
        String name = txtName.getText();
        String priceText = txtPrice.getText();
        String qtyText = txtQty.getText();

        if (name.isEmpty() || priceText.isEmpty() || qtyText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Mandatory fields are empty!").show();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int qty = Integer.parseInt(qtyText);

            boolean isSaved = productBO.saveProduct(new ProductDto(id, name, qty, price));
            if (isSaved) {
                new Alert(Alert.AlertType.INFORMATION, "Product Saved!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save product!").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input for price or quantity!").show();
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        String id = currentProductId;
        String name = txtName.getText();
        String priceText = txtPrice.getText();
        String qtyText = txtQty.getText();

        try {
            double price = Double.parseDouble(priceText);
            int qty = Integer.parseInt(qtyText);

            boolean isUpdated = productBO.updateProduct(new ProductDto(id, name, qty, price));
            if (isUpdated) {
                new Alert(Alert.AlertType.INFORMATION, "Product Updated!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update product!").show();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid input for price or quantity!").show();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Are you sure you want to delete this product?");
        confirmAlert.setContentText("Product: " + txtName.getText() + " (" + currentProductId + ")");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isDeleted = productBO.deleteProduct(currentProductId);
            if (isDeleted) {
                new Alert(Alert.AlertType.INFORMATION, "Product Deleted!").show();
                loadAllProducts();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to delete product!").show();
            }
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        txtName.clear();
        txtPrice.clear();
        txtQty.clear();
    }

    @FXML
    void txtSearchOnAction(ActionEvent event) {
        String searchTerm = txtSearch.getText().toLowerCase();
        ObservableList<ProductDto> allProducts = tblProduct.getItems();
        ProductDto foundProduct = null;

        for (ProductDto dto : allProducts) {
            if (dto.getId().toLowerCase().equals(searchTerm) ||
                    dto.getName().toLowerCase().contains(searchTerm)) {
                foundProduct = dto;
                break;
            }
        }

        if (foundProduct != null) {
            currentProductId = foundProduct.getId();
            txtName.setText(foundProduct.getName());
            txtPrice.setText(String.valueOf(foundProduct.getPrice()));
            txtQty.setText(String.valueOf(foundProduct.getQty()));
            tblProduct.getSelectionModel().select(foundProduct);
            tblProduct.scrollTo(foundProduct);
        } else {
            new Alert(Alert.AlertType.WARNING, "Product not found!").show();
        }
    }
}