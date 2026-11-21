package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.bo.custom.SupplierBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AuditCell;
import com.dev09.kumaracoconutoil.util.ProductSelectionDialog;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

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
    @FXML private AnchorPane formPane;
    @FXML private TableColumn<SupplierDto, String> colContact;
    @FXML private TableColumn<SupplierDto, String> colName;
    @FXML private TableColumn<SupplierDto, String> colCreated;
    @FXML private TableColumn<SupplierDto, String> colUpdated;
    @FXML private TableView<SupplierDto> tblSupplier;
    @FXML private TableColumn<ProductDto, String> colProdItemCode;
    @FXML private TableColumn<ProductDto, String> colProdName;
    @FXML private TableColumn<ProductDto, String> colProdPrice;
    @FXML private TableColumn<ProductDto, Double> colProdQty;
    @FXML private TableColumn<ProductDto, Void> colProdQty1;
    @FXML private TableView<ProductDto> tblSuppliersProduct;
    @FXML private Button btnAddProduct;
    @FXML private TextField txtContact;
    @FXML private TextField txtName;
    @FXML private TextField txtSearch;
    @FXML private Button btnClear;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;

    private final SupplierBO supplierBO = (SupplierBO) BOFactory.getInstance().getBo(BOFactory.BOType.SUPPLIER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
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

        setupProductTable();

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
        javafx.application.Platform.runLater(() -> txtName.requestFocus());

        tblSupplier.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentSupplierId = newValue.getSupplierId();
                txtName.setText(newValue.getName());
                txtContact.setText(newValue.getContact());
                loadSupplierProducts(newValue.getSupplierId());
                btnAddProduct.setDisable(false);

                btnSave.setDisable(true);
                boolean isCashier = DashboardController.userData != null && RoleUtil.isCashier(DashboardController.userData.getRole());
                btnUpdate.setDisable(isCashier);
                btnDelete.setDisable(isCashier);
            } else {
                loadSupplierProducts(null);
                btnAddProduct.setDisable(true);
            }
        });

        setupOutsideClickClear();
    }

    /** Clicking the empty background clears the supplier selection (same as Inventory). */
    private void setupOutsideClickClear() {
        rootPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node target)) return;
            if (isInside(target, tblSupplier) || isInside(target, tblSuppliersProduct)
                    || isInside(target, formPane) || isInside(target, btnAddProduct)) {
                return;
            }
            tblSupplier.getSelectionModel().clearSelection();
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

    private void setupProductTable() {
        colProdItemCode.setCellValueFactory(new PropertyValueFactory<>("itemcode"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("priceArray"));
        colProdQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblSuppliersProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSuppliersProduct.setPlaceholder(new Label("Select a supplier to see its products"));
        tblSuppliersProduct.getColumns().forEach(column -> column.setSortable(false));

        colProdQty1.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button();

            {
                removeButton.setGraphic(createRemoveIcon());
                removeButton.setTooltip(new Tooltip("Remove from supplier"));
                removeButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 6 4 6; -fx-cursor: hand;");
                removeButton.setOnAction(event -> removeProductFromSupplier(getTableRow() == null ? null : getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : removeButton);
            }
        });
    }

    /** Small white trash-can icon used by the product row's remove button. */
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

    /** Detach a product from the currently selected supplier and refresh the tables. */
    private void removeProductFromSupplier(ProductDto product) {
        if (product == null || currentSupplierId == null || currentSupplierId.isBlank()) {
            return;
        }
        if (product.getId() == null || product.getId().isBlank()) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update supplier!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Product");
        confirm.setHeaderText("Remove this product from the supplier?");
        confirm.setContentText("Product: " + product.getName() + " (" + product.getItemcode() + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String supplierId = currentSupplierId;
        if (productBO.assignSupplier(product.getId(), null)) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product Updated!");
            reloadSupplierView(supplierId);
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update supplier!");
        }
    }

    /** Reload supplier data and its product table, keeping the same supplier selected. */
    private void reloadSupplierView(String supplierId) {
        loadAllSuppliers();
        if (supplierId == null || supplierId.isBlank()) return;
        for (SupplierDto dto : tblSupplier.getItems()) {
            if (supplierId.equals(dto.getSupplierId())) {
                currentSupplierId = supplierId;
                tblSupplier.getSelectionModel().select(dto);
                tblSupplier.scrollTo(dto);
                return; // the selection listener repopulates the product table
            }
        }
        loadSupplierProducts(supplierId);
    }

    /** Load the products that belong to the selected supplier. */
    private void loadSupplierProducts(String supplierId) {
        ObservableList<ProductDto> items = tblSuppliersProduct.getItems();
        items.clear();
        if (supplierId == null || supplierId.isBlank()) {
            return;
        }
        ArrayList<ProductDto> products = productBO.getProductsBySupplier(supplierId);
        if (products != null) {
            items.addAll(products);
        }
    }

    /** "+ Add Product": pick an existing product and attach it to this supplier. */
    @FXML
    void btnAddProductOnAction(ActionEvent event) {
        if (currentSupplierId == null || currentSupplierId.isBlank()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a supplier first!");
            return;
        }
        Window owner = rootPane.getScene() == null ? null : rootPane.getScene().getWindow();
        Optional<ProductDto> chosen = ProductSelectionDialog.show(owner);
        if (chosen.isEmpty()) {
            return;
        }
        ProductDto product = chosen.get();
        if (productBO.assignSupplier(product.getId(), currentSupplierId)) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Product added to supplier!");
            loadSupplierProducts(currentSupplierId);
        } else {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to add product to supplier!");
        }
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
        if (AppConstants.DEFAULT_SUPPLIER_ID.equals(currentSupplierId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Default supplier cannot be updated!");
            return;
        }
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
        if (AppConstants.DEFAULT_SUPPLIER_ID.equals(currentSupplierId)) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Default supplier cannot be deleted!");
            return;
        }
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
        btnAddProduct.setDisable(true);
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

        ButtonType btn80mm = new ButtonType("Small (80mm) - Thermal Printer");
        ButtonType btnA4 = new ButtonType("A4 Page (PDF)");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btn80mm, btnA4, btnCancel);

        var result = alert.showAndWait();
        if (result.isPresent()) {
            var suppliers = supplierBO.getAllSuppliersEntity();
            if (result.get() == btn80mm) {
                com.dev09.kumaracoconutoil.util.ThermalPrinter.print(
                        com.dev09.kumaracoconutoil.util.PdfGenerator.SupplierReportThermal(suppliers));
            } else if (result.get() == btnA4) {
                byte[] bytes = com.dev09.kumaracoconutoil.util.PdfGenerator.SupplierReportA4Pro(suppliers);
                File tempFile = File.createTempFile("SupplierReport_A4", ".pdf");
                tempFile.deleteOnExit();
                Files.write(tempFile.toPath(), bytes);
                Desktop.getDesktop().open(tempFile);
            }
        }
    }
}
