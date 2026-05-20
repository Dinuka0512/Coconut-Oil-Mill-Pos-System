package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.util.PdfGenerator;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuotationController implements Initializable {

    @FXML
    private AnchorPane body;

    @FXML
    private AnchorPane quotationContent;

    @FXML
    private ComboBox<String> cmbCustomerId;

    @FXML
    private ComboBox<String> cmbProductId;

    @FXML
    private TableColumn<CartTM, String> colDescription;

    @FXML
    private TableColumn<CartTM, Integer> colQty;

    @FXML
    private TableColumn<CartTM, Double> colTotal;

    @FXML
    private TableColumn<CartTM, Double> colUnitPrice;

    @FXML
    private TableColumn<CartTM, Button> colAction;

    @FXML
    private TextField txtTax;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblQuotationId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private Label lblDate;

    @FXML
    private TableView<CartTM> tblCart;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtDiscount;

    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);

    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private java.util.List<CustomerDto> currentCustomers;
    private java.util.List<ProductDto> currentProducts;
    private CustomerDto selectedCustomer;
    private ProductDto selectedProduct;
    private boolean isContentLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isContentLoading) return;
        isContentLoading = true;
        loadContent();
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblCart.setItems(obList);

        lblDate.setText(String.valueOf(LocalDate.now()));
        setQuotationId();
        loadCustomerIds();
        loadProductIds();

        txtDiscount.setText("0");
        txtTax.setText("0");
    }

    private void loadContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/quotation_content.fxml"));
            loader.setController(this);
            isContentLoading = true;
            javafx.scene.Parent root = loader.load();
            quotationContent.getChildren().clear();
            quotationContent.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isContentLoading = false;
        }
    }

    private void setQuotationId() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        lblQuotationId.setText("QT-" + ts);
    }

    private void loadCustomerIds() {
        currentCustomers = customerBO.getAllCustomers();
        int index = 0;
        for (CustomerDto dto : currentCustomers) {
            cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
            if ("CUST001".equals(dto.getCustId())) {
                selectedCustomer = dto;
                cmbCustomerId.getSelectionModel().select(index);
            }
            index++;
        }
    }

    private void loadProductIds() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProductId.getItems().add(dto.getName() + " (" + dto.getItemcode() + ")");
        }
    }

    @FXML
    void btnNewCustomerOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Customer");
            stage.setResizable(false);
            stage.showAndWait();

            cmbCustomerId.getItems().clear();
            loadCustomerIds();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnNewProductOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/inventory.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Inventory");
            stage.setResizable(false);
            stage.showAndWait();

            cmbProductId.getItems().clear();
            loadProductIds();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage()).show();
        }
    }

    @FXML
    void cmbCustomerOnAction(ActionEvent event) {
        String selection = cmbCustomerId.getValue();
        if (selection != null && currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                if ((dto.getName() + " - " + dto.getContact()).equals(selection)) {
                    selectedCustomer = dto;
                    break;
                }
            }
        }
    }

    private Timeline custResetTimer;

    @FXML
    void cmbCustomerOnKeyReleased(javafx.scene.input.KeyEvent event) {
        String searchText = cmbCustomerId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbCustomerId.getItems().clear();
            for (CustomerDto dto : currentCustomers) {
                cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
            }
            if (custResetTimer != null) custResetTimer.stop();
        } else {
            cmbCustomerId.getItems().clear();
            for (CustomerDto dto : currentCustomers) {
                String display = dto.getName() + " - " + dto.getContact();
                if (dto.getName().toLowerCase().contains(searchText) ||
                    dto.getContact().toLowerCase().contains(searchText)) {
                    cmbCustomerId.getItems().add(display);
                }
            }
            cmbCustomerId.show();

            if (custResetTimer != null) custResetTimer.stop();
            custResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbCustomerId.getItems().isEmpty()) {
                    cmbCustomerId.getEditor().setText("");
                    cmbCustomerId.getItems().clear();
                    for (CustomerDto dto : currentCustomers) {
                        cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
                    }
                }
            }));
            custResetTimer.setCycleCount(1);
            custResetTimer.play();
        }
    }

    @FXML
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProductId.getValue();
        if (selection != null && currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + dto.getItemcode() + ")";
                if (display.equals(selection) || dto.getName().equals(selection)) {
                    selectedProduct = dto;
                    txtUnitPrice.setText(String.valueOf(dto.getPrice()));
                    lblQtyOnHand.setText(dto.getQty() + " Kg");
                    txtQty.requestFocus();
                    break;
                }
            }
        }
    }

    private Timeline resetTimer;

    @FXML
    void cmbProductOnKeyReleased(javafx.scene.input.KeyEvent event) {
        String searchText = cmbProductId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                cmbProductId.getItems().add(dto.getName() + " (" + dto.getItemcode() + ")");
            }
            if (resetTimer != null) resetTimer.stop();
        } else {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + dto.getItemcode() + ")";
                if (dto.getName().toLowerCase().contains(searchText) ||
                    dto.getItemcode().toLowerCase().contains(searchText)) {
                    cmbProductId.getItems().add(display);
                }
            }
            cmbProductId.show();

            if (resetTimer != null) resetTimer.stop();
            resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProductId.getItems().isEmpty()) {
                    cmbProductId.getEditor().setText("");
                    cmbProductId.getItems().clear();
                    for (ProductDto dto : currentProducts) {
                        cmbProductId.getItems().add(dto.getName() + " (" + dto.getItemcode() + ")");
                    }
                }
            }));
            resetTimer.setCycleCount(1);
            resetTimer.play();
        }
    }

    @FXML
    void btnAddToCartOnAction(ActionEvent event) {
        if (selectedProduct == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an inventory first!").show();
            return;
        }
        String code = selectedProduct.getId();
        String description = selectedProduct.getName();
        double availableQty = selectedProduct.getQty();
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Please enter a valid quantity!").show();
            return;
        }

        if (qty <= 0) {
            new Alert(Alert.AlertType.WARNING, "Quantity must be greater than zero!").show();
            return;
        }

        try {
            String priceStr = txtUnitPrice.getText();
            double unitPrice = Double.parseDouble(priceStr.trim());
            double total = qty * unitPrice;
            Button btn = new Button("Remove");
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

            btn.setOnAction(e -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to remove?", yes, no).showAndWait();

                if (result.orElse(no) == yes) {
                    for (int i = 0; i < obList.size(); i++) {
                        if (obList.get(i).getBtn() == btn) {
                            obList.remove(i);
                            calculateTotal();
                            break;
                        }
                    }
                }
            });

            for (int i = 0; i < obList.size(); i++) {
                if (code.equals(obList.get(i).getCode())) {
                    double newQty = qty + obList.get(i).getQty();
                    if (newQty > availableQty) {
                        new Alert(Alert.AlertType.WARNING, "Insufficient stock! Available: " + availableQty + " Kg").show();
                        return;
                    }
                    qty = newQty;
                    total = unitPrice * qty;

                    obList.get(i).setQty(qty);
                    obList.get(i).setTotal(total);

                    tblCart.refresh();
                    calculateTotal();
                    txtQty.clear();
                    return;
                }
            }

            if (qty > availableQty) {
                new Alert(Alert.AlertType.WARNING, "Insufficient stock! Available: " + availableQty + " Kg").show();
                return;
            }

            obList.add(new CartTM(code, description, qty, unitPrice, total, btn));
            calculateTotal();
            txtQty.clear();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Invalid unit price!").show();
        }
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }

        double discountPercent = 0.0;
        try {
            String discountText = txtDiscount.getText();
            if (discountText != null && !discountText.isEmpty()) {
                discountPercent = Double.parseDouble(discountText);
                if (discountPercent < 0) discountPercent = 0;
            }
        } catch (NumberFormatException e) {
            discountPercent = 0.0;
        }

        double discountAmount = netTotal * (discountPercent / 100.0);
        double totalAfterDiscount = netTotal - discountAmount;

        double taxPercent = 0.0;
        try {
            String taxText = txtTax.getText();
            if (taxText != null && !taxText.isEmpty()) {
                taxPercent = Double.parseDouble(taxText);
            }
        } catch (NumberFormatException e) {
            taxPercent = 0.0;
        }

        double taxAmount = totalAfterDiscount * (taxPercent / 100.0);
        double finalTotal = totalAfterDiscount + taxAmount;

        lblNetTotal.setText(String.format("%.2f", finalTotal));
    }

    @FXML
    void txtDiscountOnKeyReleased(javafx.scene.input.KeyEvent event) {
        calculateTotal();
    }

    @FXML
    void txtTaxOnKeyReleased(javafx.scene.input.KeyEvent event) {
        calculateTotal();
    }

    @FXML
    void btnGeneratePdfOnAction(ActionEvent event) {
        if (obList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Cart is empty!").show();
            return;
        }

        String quotationId = lblQuotationId.getText();
        LocalDate date = LocalDate.parse(lblDate.getText());

        double discount = 0.0;
        double tax = 0.0;

        try {
            discount = Double.parseDouble(txtDiscount.getText());
        } catch (NumberFormatException e) {
        }
        try {
            tax = Double.parseDouble(txtTax.getText());
        } catch (NumberFormatException e) {
        }

        String customerName = selectedCustomer != null ? selectedCustomer.getName() : "-";
        String customerContact = selectedCustomer != null ? selectedCustomer.getContact() : null;

        Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
        printAlert.setTitle("Generate Quotation PDF");
        printAlert.setHeaderText("Choose PDF format:");
        printAlert.setContentText("Select the format for the quotation PDF:");

        ButtonType smallBtn = new ButtonType("Small (80mm) PDF", ButtonBar.ButtonData.YES);
        ButtonType a4Btn = new ButtonType("A4 PDF", ButtonBar.ButtonData.NO);
        printAlert.getButtonTypes().setAll(smallBtn, a4Btn);

        Optional<ButtonType> result = printAlert.showAndWait();
        if (result.isPresent()) {
            try {
                byte[] pdfBytes;
                if (result.get() == smallBtn) {
                    pdfBytes = PdfGenerator.generateQuotationBill80mm(
                            quotationId, date, customerName, customerContact,
                            new ArrayList<>(obList), discount, tax);
                } else {
                    pdfBytes = PdfGenerator.generateQuotationBillA4Pro(
                            quotationId, date, customerName, customerContact,
                            new ArrayList<>(obList), discount, tax);
                }

                if (pdfBytes != null) {
                    File tempFile = File.createTempFile("Quotation_" + quotationId, ".pdf");
                    Files.write(tempFile.toPath(), pdfBytes);
                    Desktop.getDesktop().open(tempFile);
                    resetForm();
                }
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error generating PDF: " + e.getMessage()).show();
            }
        }
    }

    private void clearProductFields() {
        cmbProductId.getSelectionModel().clearSelection();
        selectedProduct = null;
        txtUnitPrice.clear();
        lblQtyOnHand.setText("");
        txtQty.clear();
    }

    private void resetForm() {
        obList.clear();
        tblCart.refresh();

        setQuotationId();
        lblDate.setText(String.valueOf(LocalDate.now()));

        cmbCustomerId.getItems().clear();
        cmbCustomerId.getSelectionModel().clearSelection();
        loadCustomerIds();
        selectedCustomer = null;

        cmbProductId.getItems().clear();
        cmbProductId.getSelectionModel().clearSelection();
        loadProductIds();
        selectedProduct = null;
        clearProductFields();

        txtDiscount.setText("0");
        txtTax.setText("0");
        lblNetTotal.setText("0.00");
    }

    @FXML
    void btnBackOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            if (body != null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) body.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
