package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AuditCell;
import com.dev09.kumaracoconutoil.util.PriceQtyArray;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import com.dev09.kumaracoconutoil.util.SupplierSelectionDialog;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.File;
import java.nio.file.Files;
import java.awt.Desktop;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

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

    /** Each row is a {price, qty} pair from the product's 2D array. */
    @FXML
    private TableView<double[]> productpriceArrayTbl;

    @FXML
    private TableColumn<double[], Double> colPrice1;

    @FXML
    private TableColumn<double[], Void> colAction1;

    @FXML
    private TableColumn<double[], Double> colStock1;

    @FXML
    private Button btnAddPriceStock;

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

    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
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

        setupPriceTable();

        btnSave.setTooltip(new Tooltip("Save (Ctrl+S)"));
        btnUpdate.setTooltip(new Tooltip("Update (Ctrl+U)"));
        btnDelete.setTooltip(new Tooltip("Delete (Ctrl+D)"));
        btnClear.setTooltip(new Tooltip("Clear (Ctrl+C)"));
        btnAddPriceStock.setTooltip(new Tooltip("Add Price (Enter on selected product)"));
        txtSearch.setTooltip(new Tooltip("Search Inventory (Ctrl+F)"));

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

                if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
                    btnDelete.setDisable(true);
                    btnUpdate.setDisable(true);
                } else {
                    btnDelete.setDisable(false);
                    btnUpdate.setDisable(false);
                }
                btnSave.setDisable(true);
                btnAddPriceStock.setDisable(false);
            } else {
                clearPriceDetails();
            }
        });
    }

    private void setupOutsideClickClear() {
        rootPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node target)) return;
            if (isInside(target, tblProduct) || isInside(target, productpriceArrayTbl)
                    || isInside(target, formPane) || isInside(target, btnAddPriceStock)) {
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
        txtItemCode.clear();
        txtName.clear();
        txtPrice.clear();
        txtQty.clear();
        btnAddPriceStock.setDisable(true);
    }

    private void setupPriceTable() {
        colPrice1.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(rowValue(cell.getValue(), PriceQtyArray.PRICE)));
        colStock1.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(rowValue(cell.getValue(), PriceQtyArray.QTY)));

        productpriceArrayTbl.setEditable(false);
        productpriceArrayTbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productpriceArrayTbl.getColumns().forEach(column -> column.setSortable(false));

        colAction1.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button removeButton = new Button();
            private final HBox actions = new HBox(4, editButton, removeButton);

            {
                editButton.setGraphic(createEditIcon());
                editButton.setTooltip(new Tooltip("Edit price"));
                editButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 6 4 6; -fx-cursor: hand;");
                editButton.setOnAction(event -> editPriceRow(getTableRow() == null ? null : getTableRow().getItem()));

                removeButton.setGraphic(createRemoveIcon());
                removeButton.setTooltip(new Tooltip("Remove price"));
                removeButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 6 4 6; -fx-cursor: hand;");
                removeButton.setOnAction(event -> removePriceRow(getTableRow() == null ? null : getTableRow().getItem()));

                actions.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : actions);
            }
        });
    }

    /** Small white pencil icon used by the price row's edit button. */
    private Node createEditIcon() {
        Line shaft = new Line(4, 10, 11, 3);
        shaft.setStroke(Color.WHITE);
        shaft.setStrokeWidth(2.2);

        Polygon nib = new Polygon(2.0, 12.0, 2.8, 8.8, 5.2, 11.2);
        nib.setFill(Color.WHITE);

        Group icon = new Group(shaft, nib);
        icon.setFocusTraversable(false);
        return icon;
    }

    /** Small white trash-can icon used by the price row's remove button. */
    private Node createRemoveIcon() {
        Rectangle lid = new Rectangle(1.5, 2.5, 11, 1.5);
        lid.setArcWidth(1.2);
        lid.setArcHeight(1.2);
        lid.setFill(Color.WHITE);

        Rectangle handle = new Rectangle(5, 1, 4, 1.5);
        handle.setArcWidth(0.8);
        handle.setArcHeight(0.8);
        handle.setFill(Color.WHITE);

        Rectangle body = new Rectangle(3, 4.5, 8, 8.5);
        body.setArcWidth(1.5);
        body.setArcHeight(1.5);
        body.setFill(Color.TRANSPARENT);
        body.setStroke(Color.WHITE);
        body.setStrokeWidth(1.2);

        Line leftRib = new Line(5.6, 6.5, 5.6, 11);
        Line rightRib = new Line(8.4, 6.5, 8.4, 11);
        leftRib.setStroke(Color.WHITE);
        rightRib.setStroke(Color.WHITE);
        leftRib.setStrokeWidth(1);
        rightRib.setStrokeWidth(1);

        Group icon = new Group(lid, handle, body, leftRib, rightRib);
        icon.setFocusTraversable(false);
        return icon;
    }

    private double rowValue(double[] row, int index) {
        return row != null && row.length > index ? row[index] : 0.0;
    }

    private void loadProductPrices(ProductDto product) {
        ObservableList<double[]> items = productpriceArrayTbl.getItems();
        items.clear();
        if (product == null) {
            txtPrice.clear();
            return;
        }

        double[][] pairs = product.getPriceQtyPairs();
        if (pairs.length == 0) {
            pairs = new double[][]{{product.getPrice(), 0.0}};
        }
        items.addAll(Arrays.asList(pairs));
        txtPrice.setText(formatPrice(pairs[0][PriceQtyArray.PRICE]));
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
        btnAddPriceStock.setDisable(true);
        btnSave.setDisable(false);
    }

    private void loadAllProducts() {
        tblProduct.getSelectionModel().clearSelection();
        ObservableList<ProductDto> items = tblProduct.getItems();
        items.clear();
        ArrayList<ProductDto> allProducts = productBO.getAllProducts();
        if (allProducts != null) {
            items.addAll(allProducts);
        }
    }

    private void generateNextId() {
        currentProductId = productBO.generateNewProductId();
        txtName.setText("");
    }

    /** Opens the modal and returns true only when a price/stock pair was added. */
    private boolean openAddPriceStockForm(String productName, String productId) {
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

            boolean added = addPriceStockController.isPriceAdded();
            if (added) {
                refreshPricesAfterAdd(productId);
            }
            return added;
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open add price screen!");
            return false;
        }
    }

    /** Reload the product after a price/qty pair was appended by the modal form. */
    private void refreshPricesAfterAdd(String productId) {
        if (productId == null || productId.isEmpty()) {
            return;
        }
        ProductDto refreshed = productBO.searchProduct(productId);
        if (refreshed != null) {
            loadProductPrices(refreshed);
            txtQty.setText(String.valueOf(refreshed.getQty()));
            updateProductRow(refreshed);
        }
    }

    /**
     * Keep the main inventory table in sync after a price/stock pair was added.
     * The row keeps its identity but its price array, stock and audit info are
     * copied from the freshly loaded product, then the table is re-rendered.
     */
    private void updateProductRow(ProductDto refreshed) {
        if (refreshed == null || refreshed.getId() == null) {
            return;
        }
        for (ProductDto row : tblProduct.getItems()) {
            if (refreshed.getId().equals(row.getId())) {
                row.setPriceQty(refreshed.getPriceQty());
                row.setUpdatedBy(refreshed.getUpdatedBy());
                row.setUpdatedAt(refreshed.getUpdatedAt());
                break;
            }
        }
        tblProduct.refresh();
    }

    private boolean confirmAddPrice() {
        Alert addPriceAlert = new Alert(Alert.AlertType.CONFIRMATION);
        addPriceAlert.setTitle("Add Price");
        addPriceAlert.setHeaderText(null);
        addPriceAlert.setContentText("Do you want to add new price and stock?");
        ButtonType addYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType addNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        // A CONFIRMATION alert already has OK/Cancel; replace them, don't append.
        addPriceAlert.getButtonTypes().setAll(addYes, addNo);
        Optional<ButtonType> addResult = addPriceAlert.showAndWait();
        return addResult.isPresent() && addYes.equals(addResult.get());
    }

    /**
     * Ask before opening the add-price form. After Save, keep asking after every
     * successful add so several price/stock pairs can be entered in one session.
     *
     * @return true only when the user answered No to the add-price question.
     */
    private boolean askAndOpenAddPriceForm(String productName, String productId, boolean repeatAfterAdd) {
        do {
            if (!confirmAddPrice()) {
                return true; // user said No
            }
            if (!openAddPriceStockForm(productName, productId)) {
                return false; // closed without adding a price
            }
        } while (repeatAfterAdd);
        return false;
    }

    /**
     * Attach a supplier to a freshly saved product. The user picks the supplier
     * with the supplier picker, defaulting to the built-in supplier if cancelled.
     */
    private void assignSupplierToProduct(String productId) {
        SupplierDto chosen = openSupplierSelection();
        String supplierId = chosen == null ? AppConstants.DEFAULT_SUPPLIER_ID : chosen.getSupplierId();
        if (supplierId != null && !supplierId.isBlank()) {
            productBO.assignSupplier(productId, supplierId);
        }
    }

    /** Opens the supplier picker and returns the chosen supplier (null when cancelled). */
    private SupplierDto openSupplierSelection() {
        try {
            Window owner = rootPane.getScene() == null ? null : rootPane.getScene().getWindow();
            return SupplierSelectionDialog.show(owner).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open supplier selection!");
            return null;
        }
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        String id = currentProductId;
        String itemcode = txtItemCode.getText().trim();
        String name = txtName.getText().trim();
        String priceText = txtPrice.getText().trim();
        String qtyText = txtQty.getText().trim();
        ObservableList<double[]> priceRows = productpriceArrayTbl.getItems();

        if (itemcode.isEmpty() || name.isEmpty() || qtyText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Mandatory fields are empty!");
            return;
        }
        if (priceText.isEmpty() && priceRows.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Add at least one price for the inventory item!");
            return;
        }
        if (productBO.existActiveProductByItemCode(itemcode)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Item Code already exists! Please use a unique Item Code.");
            return;
        }

        try {
            double[][] pairs = buildPriceQty(priceText, priceRows);
            if (pairs == null) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price!");
                return;
            }

            // If a soft-deleted product already uses this item code, restore it under
            // its existing id so the price/supplier steps below target the right row.
            String existingId = productBO.findAnyProductIdByItemCode(itemcode);
            if (existingId != null) {
                id = existingId;
            }

            boolean isSaved = productBO.saveProduct(new ProductDto(id, itemcode, name, pairs));
            if (isSaved) {
                loadAllProducts();
                clearFields();
                askAndOpenAddPriceForm(name, id, true);
                assignSupplierToProduct(id);
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
        ObservableList<double[]> priceRows = productpriceArrayTbl.getItems();

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
            double[][] pairs = buildPriceQty(priceText, priceRows);
            if (pairs == null) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for price!");
                return;
            }

            boolean isUpdated = productBO.updateProduct(new ProductDto(id, itemcode, name, pairs));
            if (isUpdated) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
                loadAllProducts();
                clearFields();
                askAndOpenAddPriceForm(name, id, false);
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid input for quantity!");
        }
    }

    /**
     * Build the 2D price/qty array from the editor. The typed price is the primary
     * (first) price and the typed quantity is the total stock across all rows.
     */
    private double[][] buildPriceQty(String priceText, ObservableList<double[]> rows) {
        Double primaryPrice;
        if (priceText != null && !priceText.isBlank()) {
            primaryPrice = parsePrice(priceText);
            if (primaryPrice == null) return null;
        } else if (!rows.isEmpty()) {
            primaryPrice = rowValue(rows.get(0), PriceQtyArray.PRICE);
        } else {
            return null;
        }
        if (primaryPrice < 0) return null;

        double totalQty = productStockValue();
        if (rows.isEmpty()) {
            return PriceQtyArray.normalize(new double[][]{{primaryPrice, totalQty}});
        }

        double[][] pairs = new double[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            double[] row = rows.get(i);
            pairs[i] = row == null ? new double[2] : row.clone();
        }
        pairs[0][PriceQtyArray.PRICE] = primaryPrice;
        return PriceQtyArray.withTotalQty(pairs, totalQty);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can delete inventory!");
            return;
        }
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

    /** Enter on a selected product row opens the "+ Add Price" form. */
    @FXML
    void tblProductOnKeyPressed(KeyEvent event) {
        if (event.getCode() != KeyCode.ENTER) return;
        if (tblProduct.getSelectionModel().getSelectedItem() == null || btnAddPriceStock.isDisabled()) return;
        event.consume();
        btnAddPriceStock.fire();
    }

    @FXML
    void btnAddPriceStockOnAction(ActionEvent event) {
        if (currentProductId == null || currentProductId.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
            return;
        }
        String productName = txtName.getText() == null ? "" : txtName.getText().trim();
        openAddPriceStockForm(productName, currentProductId);
    }

    /** Remove a price tier, save the change to the database, then reload both tables. */
    private void removePriceRow(double[] row) {
        if (row == null) return;
        ProductDto selected = tblProduct.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
            return;
        }
        ObservableList<double[]> items = productpriceArrayTbl.getItems();
        if (items.size() <= 1) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cannot remove the last price!");
            return;
        }
        if (!items.remove(row)) {
            return;
        }

        ProductDto saved = productBO.searchProduct(selected.getId());
        if (saved == null) {
            reloadProductView(selected.getId());
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
            return;
        }

        double[][] remaining = new double[items.size()][];
        for (int i = 0; i < items.size(); i++) {
            remaining[i] = items.get(i);
        }
        ProductDto changed = new ProductDto(saved.getId(), saved.getItemcode(), saved.getName(), remaining);
        if (!productBO.updateProduct(changed)) {
            reloadProductView(saved.getId());
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
            return;
        }

        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
        reloadProductView(saved.getId());
    }

    /** Open the add-price form pre-filled so one selected price row can be edited. */
    private void editPriceRow(double[] row) {
        if (row == null) return;
        ProductDto selected = tblProduct.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getId() == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
            return;
        }
        int index = priceRowIndex(row);
        if (index < 0) return;

        String productName = selected.getName() == null ? "" : selected.getName();
        openEditPriceStockForm(productName, selected.getId(), index,
                rowValue(row, PriceQtyArray.PRICE), rowValue(row, PriceQtyArray.QTY));
    }

    /** Position of this price row in the table; identities are checked before values. */
    private int priceRowIndex(double[] row) {
        ObservableList<double[]> items = productpriceArrayTbl.getItems();
        for (int i = 0; i < items.size(); i++) {
            double[] item = items.get(i);
            if (item == row || (item != null && Arrays.equals(item, row))) {
                return i;
            }
        }
        return -1;
    }

    /** Save the edited price/stock pair, show the update alert, then reload the tables. */
    private void openEditPriceStockForm(String productName, String productId, int index,
                                        double oldPrice, double oldQty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/addPriceStock.fxml"));
            Parent root = loader.load();
            AddPriceStockController controller = loader.getController();
            controller.initDataForEdit(productId, productName, oldPrice, oldQty);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Price & Stock");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (!controller.isPriceAdded()) return;
            Double newPrice = controller.getEditedPrice();
            Double newQty = controller.getEditedStock();
            if (newPrice == null || newQty == null) return;

            ProductDto saved = productBO.searchProduct(productId);
            double[][] pairs = saved == null ? null : saved.getPriceQtyPairs();
            if (pairs == null || index >= pairs.length) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
                reloadProductView(productId);
                return;
            }

            pairs[index] = new double[]{newPrice, newQty};
            ProductDto changed = new ProductDto(saved.getId(), saved.getItemcode(), saved.getName(), pairs);
            if (!productBO.updateProduct(changed)) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update inventory!");
                reloadProductView(productId);
                return;
            }

            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
            reloadProductView(productId);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open edit price screen!");
        }
    }

    /** Reload the inventory table and the selected product's price rows from the database. */
    private void reloadProductView(String productId) {
        loadAllProducts();
        ProductDto refreshed = productBO.searchProduct(productId);
        if (refreshed == null) return;
        for (ProductDto dto : tblProduct.getItems()) {
            if (productId.equals(dto.getId())) {
                tblProduct.getSelectionModel().select(dto);
                tblProduct.scrollTo(dto);
                return; // the selection listener repopulates the price table
            }
        }
        loadProductPrices(refreshed);
        txtQty.setText(String.valueOf(refreshed.getQty()));
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
        ObservableList<double[]> items = productpriceArrayTbl.getItems();
        if (items.isEmpty()) {
            items.add(new double[]{price, productStockValue()});
        } else {
            items.get(0)[PriceQtyArray.PRICE] = price;
        }
        productpriceArrayTbl.refresh();
    }

    private double productStockValue() {
        double stock = 0.0;
        try { stock = Double.parseDouble(txtQty.getText().trim()); } catch (NumberFormatException ignored) { }
        return stock;
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
        // Clear the row selection too, otherwise clicking the same row again
        // fires no selection-change event, so the form never re-populates.
        tblProduct.getSelectionModel().clearSelection();
        pageReset();
        generateNextId();
        clearPriceDetails();
        txtItemCode.requestFocus();
    }

    @FXML
    void txtSearchOnKeyReleased(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            txtSearch.clear();
            if (txtSearch.getParent() != null) txtSearch.getParent().requestFocus();
            loadAllProducts();
        } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            performSearchWithAlert();
        } else {
            performSearch();
        }
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
        String searchTerm = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        ArrayList<ProductDto> allProducts = productBO.getAllProducts();
        tblProduct.getSelectionModel().clearSelection();
        ObservableList<ProductDto> items = tblProduct.getItems();
        items.clear();
        if (allProducts == null) return;
        for (ProductDto dto : allProducts) {
            String itemcode = dto.getItemcode();
            if ((dto.getId() != null && dto.getId().toLowerCase().contains(searchTerm)) ||
                    (itemcode != null && itemcode.toLowerCase().contains(searchTerm)) ||
                    (dto.getName() != null && dto.getName().toLowerCase().contains(searchTerm))) {
                items.add(dto);
            }
        }
    }

    private void performSearchWithAlert() {
        String searchTerm = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        performSearch();
        if (tblProduct.getItems().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Inventory not found!");
        } else {
            selectProduct(tblProduct.getItems().get(0));
        }
    }
}
