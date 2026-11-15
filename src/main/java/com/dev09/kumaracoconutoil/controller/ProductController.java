package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditCell;
import com.dev09.kumaracoconutoil.util.RoleUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductPriceBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.ProductPriceDto;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.awt.Desktop;
import javafx.scene.control.ChoiceDialog;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TableColumn<ProductDto, String> colItemCode;

    @FXML
    private TableColumn<ProductDto, String> colName;

    @FXML
    private TableColumn<ProductDto, String> colPrice;

    @FXML
    private TableColumn<ProductDto, Double> colQty;

    @FXML
    private TableColumn<ProductDto, String> colCreated;

    @FXML
    private TableColumn<ProductDto, String> colUpdated;

    @FXML
    private TableView<ProductDto> tblProduct;

    @FXML
    private AnchorPane formPane;

    @FXML
    private AnchorPane priceDetailsPane;

    @FXML
    private TableView<ProductPriceDto> productpriceArrayTbl;

    @FXML
    private TableColumn<ProductPriceDto, String> colName1;

    @FXML
    private TableColumn<ProductPriceDto, Double> colPrice1;

    @FXML
    private TableColumn<ProductPriceDto, String> colCreated1;

    @FXML
    private TableColumn<ProductPriceDto, String> colUpdated1;

    @FXML
    private TableColumn<ProductPriceDto, Void> colAction1;

    @FXML
    private TableColumn<ProductPriceDto, Double> colStock1;

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
    private ProductPriceBO productPriceBO = (ProductPriceBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT_PRICE);
    private String currentProductId;

    private void setupShortcuts() {
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!event.isControlDown() || event.isAltDown() || event.isMetaDown()) return;
            switch (event.getCode()) {
                case S -> {
                    event.consume();
                    btnSave.fire();
                }
                case U -> {
                    event.consume();
                    btnUpdate.fire();
                }
                case D -> {
                    event.consume();
                    btnDelete.fire();
                }
                case F -> {
                    event.consume();
                    txtSearch.requestFocus();
                    txtSearch.selectAll();
                }
                case C -> {
                    event.consume();
                    btnClear.fire();
                }
                default -> { }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pageReset();
        colItemCode.setCellValueFactory(new PropertyValueFactory<>("itemcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("priceArray"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));

        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated.setCellFactory(AuditCell.factory());
        colUpdated.setCellFactory(AuditCell.factory());
        boolean canSeeAudit = DashboardController.userData != null && "admin".equals(DashboardController.userData.getRole());
        colCreated.setVisible(canSeeAudit);
        colUpdated.setVisible(canSeeAudit);
        tblProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupPriceTable(canSeeAudit);

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        txtSearch.setTooltip(new Tooltip("Search Inventory (Ctrl+F)"));

        setPriceDetailsVisible(false);
        loadAllProducts();
        generateNextId();

        setupShortcuts();
        setupOutsideClickClear();
        javafx.application.Platform.runLater(() -> txtItemCode.requestFocus());

        tblProduct.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentProductId = newValue.getId();
                txtItemCode.setText(newValue.getItemcode());
                txtName.setText(newValue.getName());
                txtQty.setText(String.valueOf(newValue.getQty()));
                loadProductPrices(newValue);
                setPriceDetailsVisible(true);

                if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
                    btnDelete.setDisable(true);
                    btnUpdate.setDisable(true);
                } else {
                    btnDelete.setDisable(false);
                    btnUpdate.setDisable(false);
                }
                btnSave.setDisable(true);
            } else {
                clearPriceDetails();
            }
        });
    }

    private void setupOutsideClickClear() {
        rootPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node target)) return;
            if (isInside(target, tblProduct) || isInside(target, priceDetailsPane) || isInside(target, formPane)) {
                return;
            }
            if (tblProduct.getSelectionModel().getSelectedItem() != null) {
                tblProduct.getSelectionModel().clearSelection();
            } else {
                clearPriceDetails();
            }
        });
    }

    private boolean isInside(Node node, Node container) {
        Node current = node;
        while (current != null) {
            if (current == container) return true;
            current = current.getParent();
        }
        return false;
    }

    private void clearPriceDetails() {
        productpriceArrayTbl.getItems().clear();
        setPriceDetailsVisible(false);
    }

    private void setupPriceTable(boolean canSeeAudit) {
        colName1.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice1.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCreated1.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated1.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated1.setCellFactory(AuditCell.factory());
        colUpdated1.setCellFactory(AuditCell.factory());
        colCreated1.setVisible(canSeeAudit);
        colUpdated1.setVisible(canSeeAudit);

        productpriceArrayTbl.setEditable(true);
        productpriceArrayTbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productpriceArrayTbl.getColumns().forEach(column -> column.setSortable(false));
        colPrice1.setEditable(true);
        StringConverter<Double> priceConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : String.format(Locale.US, "%.2f", value);
            }

            @Override
            public Double fromString(String text) {
                return parsePrice(text);
            }
        };
        colPrice1.setCellFactory(column -> {
            TextFieldTableCell<ProductPriceDto, Double> cell = new TextFieldTableCell<>(priceConverter);
            cell.setOnMouseClicked(event -> {
                if (cell.isEditing() || cell.isEmpty() || cell.getTableRow() == null) return;
                if (cell.getTableRow().getItem() == null) return;
                cell.getTableView().edit(cell.getTableRow().getIndex(), column);
            });
            return cell;
        });
        colPrice1.setOnEditCommit(event -> {
            Double value = event.getNewValue();
            if (value == null) {
                productpriceArrayTbl.refresh();
                return;
            }
            ProductPriceDto row = event.getRowValue();
            row.setPrice(value);
            if (productpriceArrayTbl.getItems().indexOf(row) == 0) {
                txtPrice.setText(formatPrice(value));
            }
            productpriceArrayTbl.refresh();
        });

        colStock1.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock1.setEditable(true);
        StringConverter<Double> stockConverter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : String.format(Locale.US, "%.2f", value);
            }

            @Override
            public Double fromString(String text) {
                if (text == null || text.isBlank()) return null;
                try {
                    double value = Double.parseDouble(text.trim());
                    return value < 0 ? null : value;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };
        colStock1.setCellFactory(column -> {
            TextFieldTableCell<ProductPriceDto, Double> cell = new TextFieldTableCell<>(stockConverter);
            cell.setOnMouseClicked(event -> {
                if (cell.isEditing() || cell.isEmpty() || cell.getTableRow() == null) return;
                if (cell.getTableRow().getItem() == null) return;
                cell.getTableView().edit(cell.getTableRow().getIndex(), column);
            });
            return cell;
        });
        colStock1.setOnEditCommit(event -> {
            Double value = event.getNewValue();
            if (value == null) {
                productpriceArrayTbl.refresh();
                return;
            }
            ProductPriceDto row = event.getRowValue();
            row.setStock(value);
            productpriceArrayTbl.refresh();
        });

        colAction1.setCellFactory(column -> new TableCell<>() {
            private final Button addButton = new Button("+ Add");
            private final Button removeButton = new Button("Remove");
            private final HBox box = new HBox(6, addButton, removeButton);

            {
                box.setAlignment(Pos.CENTER);
                addButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 8 2 8;");
                removeButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 8 2 8;");
                addButton.setOnAction(event -> addPriceRow(getTableRow() == null ? null : getTableRow().getItem()));
                removeButton.setOnAction(event -> removePriceRow(getTableRow() == null ? null : getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : box);
            }
        });
    }

    private void setPriceDetailsVisible(boolean visible) {
        priceDetailsPane.setVisible(visible);
        priceDetailsPane.setManaged(visible);
        tblProduct.setLayoutY(visible ? 430 : 258);
        tblProduct.setPrefHeight(visible ? 302 : 474);
    }

    private void loadProductPrices(ProductDto product) {
        ObservableList<ProductPriceDto> items = productpriceArrayTbl.getItems();
        items.clear();
        if (product == null) {
            txtPrice.clear();
            return;
        }

        ArrayList<ProductPriceDto> prices = productPriceBO.getPricesByProduct(product.getId());
        if (prices.isEmpty()) {
            prices.add(new ProductPriceDto(null, product.getId(), product.getName(), product.getPrice()));
        }
        for (ProductPriceDto price : prices) {
            price.setProductName(product.getName());
        }
        items.setAll(prices);
        if (!prices.isEmpty()) {
            txtPrice.setText(formatPrice(prices.get(0).getPrice()));
        }
    }

    @FXML
    void exportAllItems(ActionEvent event) {
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
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
        tblProduct.getSelectionModel().clearSelection();
        ObservableList<ProductDto> items = tblProduct.getItems();
        items.clear();
        ArrayList<ProductDto> allProducts = productBO.getAllProducts();
        if (allProducts == null) return;

        Map<String, ArrayList<Double>> pricesByProduct = new HashMap<>();
        for (ProductPriceDto price : productPriceBO.getAllPrices()) {
            pricesByProduct.computeIfAbsent(price.getProductId(), key -> new ArrayList<>())
                    .add(price.getPrice());
        }
        for (ProductDto dto : allProducts) {
            ArrayList<Double> prices = pricesByProduct.get(dto.getId());
            if (prices == null || prices.isEmpty()) {
                prices = new ArrayList<>();
                prices.add(dto.getPrice());
            }
            dto.setPrices(prices);
        }
        items.addAll(allProducts);
    }

    private void generateNextId() {
        currentProductId = productBO.generateNewProductId();
        txtName.setText("");
    }

    private void openAddPriceStockForm(String productName, String productId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/addPriceStock.fxml"));
            Parent root = loader.load();
            AddPriceStockController addPriceStockController = loader.getController();
            addPriceStockController.initData(productId, productName);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Price & Stock");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open add price screen!");
        }
    }

    private void askAndOpenAddPriceForm(String productName, String productId) {
        Alert addPriceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        addPriceAlert.setTitle("Add Price");
        addPriceAlert.setHeaderText(null);
        addPriceAlert.setContentText(null);
        ButtonType addYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType addNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        addPriceAlert.getButtonTypes().addAll(addYes, addNo);
        Optional<ButtonType> addResult = addPriceAlert.showAndWait();
        if (addResult.isPresent() && addYes.getText().equals(addResult.get().getText())) {
            openAddPriceStockForm(productName, productId);
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentProductId;
        String itemcode = txtItemCode.getText().trim();
        String name = txtName.getText().trim();
        String priceText = txtPrice.getText().trim();
        String qtyText = txtQty.getText().trim();
        ObservableList<ProductPriceDto> priceRows = productpriceArrayTbl.getItems();

        if (itemcode.isEmpty() || name.isEmpty() || qtyText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Mandatory fields are empty!");
            return;
        }
        if (priceText.isEmpty() && priceRows.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Add at least one price for the inventory item!");
            return;
        }

        if (productBO.existProductByItemCode(itemcode)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.");
            return;
        }

        try {
            Double primaryPrice = resolvePrimaryPrice(priceText, priceRows);
            if (primaryPrice == null) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price!");
                return;
            }

            // Product stock = sum of all price + stock pairs in the price array.
            double totalStock = 0.0;
            for (ProductPriceDto priceRow : priceRows) {
                totalStock += priceRow.getStock();
            }

            boolean isSaved = productBO.saveProduct(new ProductDto(id, itemcode, name, totalStock, primaryPrice));
            if (isSaved) {
                boolean pricesSaved = savePriceRows(id, name, priceRows);
                if (pricesSaved) {
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Saved!");
                } else {
                    AlertUtil.showAlert(Alert.AlertType.WARNING, "Product saved, but some prices could not be saved!");
                }
                loadAllProducts();
                clearFields();

                askAndOpenAddPriceForm(name, id);
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to save inventory!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for quantity!");
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can update inventory!");
            return;
        }
        String id = currentProductId;
        String itemcode = txtItemCode.getText().trim();
        String name = txtName.getText().trim();
        String priceText = txtPrice.getText().trim();
        String qtyText = txtQty.getText().trim();
        ObservableList<ProductPriceDto> priceRows = productpriceArrayTbl.getItems();

        if (itemcode.isEmpty() || name.isEmpty() || qtyText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Mandatory fields are empty!");
            return;
        }
        if (priceText.isEmpty() && priceRows.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Add at least one price for the inventory item!");
            return;
        }

        ProductDto currentProduct = productBO.searchProduct(id);
        if (currentProduct != null && !itemcode.equals(currentProduct.getItemcode()) && productBO.existProductByItemCode(itemcode)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.");
            return;
        }

        try {
            Double primaryPrice = resolvePrimaryPrice(priceText, priceRows);
            if (primaryPrice == null) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price!");
                return;
            }

            // Product stock = sum of all price + stock pairs in the price array.
            double totalStock = 0.0;
            for (ProductPriceDto priceRow : priceRows) {
                totalStock += priceRow.getStock();
            }

            boolean isUpdated = productBO.updateProduct(new ProductDto(id, itemcode, name, totalStock, primaryPrice));
            if (isUpdated) {
                boolean pricesSaved = savePriceRows(id, name, priceRows);
                if (pricesSaved) {
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
                } else {
                    AlertUtil.showAlert(Alert.AlertType.WARNING, "Product updated, but some prices could not be saved!");
                }
                loadAllProducts();
                clearFields();

                askAndOpenAddPriceForm(name, id);
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for quantity!");
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
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
                productPriceBO.deletePricesByProduct(currentProductId);
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Deleted!");
                loadAllProducts();
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete inventory!");
            }
        }
    }

    @FXML
    void btnAddPriceOnAction(ActionEvent event) {
        addPriceRow(null);
    }

    private void addPriceRow(ProductPriceDto after) {
        ObservableList<ProductPriceDto> items = productpriceArrayTbl.getItems();
        Double price = parsePrice(txtPrice.getText());
        double newStock = 0.0;
        try { newStock = Double.parseDouble(txtQty.getText().trim()); } catch (NumberFormatException ignored) { }
        ProductPriceDto dto = new ProductPriceDto(
                null,
                currentProductId,
                txtName.getText(),
                price == null ? 0.0 : price,
                newStock
        );

        int index = items.size();
        if (after != null) {
            int currentIndex = items.indexOf(after);
            if (currentIndex >= 0) index = currentIndex + 1;
        }
        items.add(index, dto);
        if (items.size() == 1) {
            txtPrice.setText(formatPrice(dto.getPrice()));
        }
        productpriceArrayTbl.getSelectionModel().select(dto);
        productpriceArrayTbl.scrollTo(dto);
        productpriceArrayTbl.edit(index, colPrice1);
    }

    private void removePriceRow(ProductPriceDto row) {
        if (row == null) return;
        ObservableList<ProductPriceDto> items = productpriceArrayTbl.getItems();
        items.remove(row);
        if (items.isEmpty()) {
            txtPrice.clear();
        } else {
            txtPrice.setText(formatPrice(items.get(0).getPrice()));
        }
        productpriceArrayTbl.getSelectionModel().clearSelection();
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
        syncPriceFieldToTable();
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

    private void syncPriceFieldToTable() {
        Double price = parsePrice(txtPrice.getText());
        if (price == null) return;
        ObservableList<ProductPriceDto> items = productpriceArrayTbl.getItems();
        if (items.isEmpty()) {
            items.add(new ProductPriceDto(null, currentProductId, txtName.getText(), price, productStockValue()));
        } else {
            items.get(0).setPrice(price);
        }
        productpriceArrayTbl.refresh();
    }

    private double productStockValue() {
        double stock = 0.0;
        try { stock = Double.parseDouble(txtQty.getText().trim()); } catch (NumberFormatException ignored) { }
        return stock;
    }

    private Double resolvePrimaryPrice(String priceText, ObservableList<ProductPriceDto> priceRows) {
        Double primaryPrice;
        if (priceText != null && !priceText.isBlank()) {
            primaryPrice = parsePrice(priceText);
            if (primaryPrice == null) return null;
        } else if (!priceRows.isEmpty()) {
            primaryPrice = priceRows.get(0).getPrice();
        } else {
            return null;
        }

        if (priceRows.isEmpty()) {
            priceRows.add(new ProductPriceDto(null, currentProductId, txtName.getText(), primaryPrice, productStockValue()));
        } else {
            priceRows.get(0).setPrice(primaryPrice);
            priceRows.get(0).setStock(productStockValue());
        }
        txtPrice.setText(formatPrice(primaryPrice));
        productpriceArrayTbl.refresh();
        return primaryPrice;
    }

    private boolean savePriceRows(String productId, String productName, ObservableList<ProductPriceDto> priceRows) {
        ArrayList<ProductPriceDto> prices = new ArrayList<>();
        for (ProductPriceDto dto : priceRows) {
            if (dto.getPrice() < 0) continue;
            dto.setProductId(productId);
            dto.setProductName(productName);
            prices.add(dto);
        }
        return productPriceBO.syncPrices(productId, prices);
    }

    private Double parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            double value = Double.parseDouble(text.trim());
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.2f", price);
    }

    private void clearFields() {
        pageReset();
        generateNextId();
        productpriceArrayTbl.getItems().clear();
        setPriceDetailsVisible(false);
        txtItemCode.clear();
        txtName.clear();
        txtPrice.clear();
        txtQty.clear();
        txtItemCode.requestFocus();
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
        txtQty.setText(String.valueOf(foundProduct.getQty()));
        loadProductPrices(foundProduct);
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
