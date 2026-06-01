package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.bo.custom.TaxBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.TaxDto;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML
    private AnchorPane body;

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
    private Label lblQtyOnHand;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private Label lblDate;

    @FXML
    private TableView<CartTM> tblOrderCart;

    @FXML
    private TextField txtQty;

    @FXML
    private Button btnUpdate;

    @FXML
    private TextField txtDiscount;

    @FXML
    private TextField txtAmount;

    @FXML
    private Label lblBalance;

    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private TaxBO taxBO = (TaxBO) BOFactory.getInstance().getBo(BOFactory.BOType.TAX);

    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private java.util.List<CustomerDto> currentCustomers;
    private java.util.List<ProductDto> currentProducts;
    private CustomerDto selectedCustomer;
    private ProductDto selectedProduct;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblOrderCart.setItems(obList);

        lblDate.setText(String.valueOf(LocalDate.now()));
        loadCustomerIds();
        loadProductIds();
        loadTax();

        txtDiscount.setText("0");
        txtAmount.setText("-");
    }

    private void loadTax() {
        try {
            TaxDto taxDto = taxBO.searchTax("1");
            txtTax.setText(taxDto != null ? String.valueOf(taxDto.getTax()) : "0");
        } catch (Exception e) {
            txtTax.setText("0");
        }
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
            cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
        }
    }

    @FXML
    void btnNewCustomerOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("New Customer");
            stage.setResizable(false);
            stage.showAndWait();

            cmbCustomerId.getItems().clear();
            loadCustomerIds();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage());
        }
    }

    @FXML
    void btnNewProductOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/inventory.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("New Inventory");
            stage.setResizable(false);
            stage.showAndWait();

            cmbProductId.getItems().clear();
            loadProductIds();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage());
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
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
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
    private Timeline custResetTimer;

    @FXML
    void cmbProductOnKeyReleased(javafx.scene.input.KeyEvent event) {
        String searchText = cmbProductId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
            if (resetTimer != null) resetTimer.stop();
        } else {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (dto.getName().toLowerCase().contains(searchText) ||
                    (dto.getItemcode() != null ? dto.getItemcode() : "").toLowerCase().contains(searchText)) {
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
                        cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
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
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a product first!");
            return;
        }
        String code = selectedProduct.getId();
        String description = selectedProduct.getName();
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText());
        } catch (NumberFormatException ex) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter a valid quantity!");
            return;
        }

        double qtyOnHand = selectedProduct.getQty();
        double existingQtyInCart = 0;
        for (int i = 0; i < obList.size(); i++) {
            if (code.equals(obList.get(i).getCode())) {
                existingQtyInCart = obList.get(i).getQty();
                break;
            }
        }

        double totalQtyInCart = existingQtyInCart + qty;
        if (totalQtyInCart > qtyOnHand) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Not enough stock!\nAvailable: " + qtyOnHand + " Kg\nIn Cart: " + existingQtyInCart + " Kg");
            return;
        }

        try {
            double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());
            double total = qty * unitPrice;
            Button btn = new Button("X");
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");

            btn.setOnAction(e -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Remove this item?", yes, no).showAndWait();
                if (result.orElse(no) == yes) {
                    obList.removeIf(item -> item.getBtn() == btn);
                    calculateTotal();
                }
            });

            for (int i = 0; i < obList.size(); i++) {
                if (code.equals(obList.get(i).getCode())) {
                    qty += obList.get(i).getQty();
                    total = unitPrice * qty;
                    obList.get(i).setQty(qty);
                    obList.get(i).setTotal(total);
                    tblOrderCart.refresh();
                    calculateTotal();
                    txtQty.clear();
                    txtQty.requestFocus();
                    return;
                }
            }

            obList.add(new CartTM(code, description, qty, unitPrice, total, btn));
            calculateTotal();
            txtQty.clear();
            txtQty.requestFocus();
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid unit price!");
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
                discountText = discountText.replace("%", "").trim();
                discountPercent = Double.parseDouble(discountText);
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
        calculateBalance();
    }

    private void calculateBalance() {
        try {
            double netTotal = Double.parseDouble(lblNetTotal.getText());
            String amountText = txtAmount.getText();

            if (amountText == null || amountText.isEmpty() || amountText.equals("-")) {
                lblBalance.setText("0.00");
                lblBalance.setStyle("-fx-text-fill: #00a118;");
                return;
            }

            double amount = Double.parseDouble(amountText);
            double balance = amount - netTotal;

            if (balance < 0) {
                lblBalance.setText("Insufficient!");
                lblBalance.setStyle("-fx-text-fill: red;");
            } else {
                lblBalance.setText(String.format("%.2f", balance));
                lblBalance.setStyle("-fx-text-fill: #00a118;");
            }
        } catch (NumberFormatException e) {
            lblBalance.setText("0.00");
        }
    }

    @FXML
    void txtAmountOnKeyReleased(javafx.scene.input.KeyEvent event) {
        calculateBalance();
    }

    @FXML
    void txtDiscountOnKeyReleased(javafx.scene.input.KeyEvent event) {
        calculateTotal();
    }

    @FXML
    void txtDiscountOnAction(ActionEvent event) {
        calculateTotal();
    }

    @FXML
    void btnPlaceOrderOnAction(ActionEvent event) {
        if (selectedCustomer == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a customer first!");
            return;
        }
        if (obList.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }

        String orderId;
        LocalDate date = LocalDate.parse(lblDate.getText());
        CustomerDto customerDto = selectedCustomer;

        double netTotal = 0.0;
        double discount = 0.0;
        double taxRate = 0.0;
        double balance = 0.0;

        try {
            orderId = orderBO.generateNextOrderId();
            netTotal = Double.parseDouble(lblNetTotal.getText());
            discount = Double.parseDouble(txtDiscount.getText());
            taxRate = Double.parseDouble(txtTax.getText());
            balance = Double.parseDouble(lblBalance.getText());
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error processing order: " + e.getMessage());
            return;
        }

        double amount = netTotal;

        ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
        for (CartTM tm : obList) {
            ProductDto productDto = productBO.searchProduct(tm.getCode());
            details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
        }

        OrderDto orderDto = new OrderDto(orderId, date, customerDto, details, amount, discount, 0, taxRate, balance);
        try {
            boolean isPlaced = orderBO.placeOrder(orderDto);
            if (isPlaced) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Invoice Placed Successfully!");

                Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
                printAlert.setTitle("Print Bill");
                printAlert.setHeaderText("Print bill?");
                printAlert.setContentText("Choose format:");

                ButtonType smallBtn = new ButtonType("Small (80mm) PDF", ButtonBar.ButtonData.YES);
                ButtonType a4Btn = new ButtonType("A4 PDF", ButtonBar.ButtonData.NO);
                printAlert.getButtonTypes().setAll(smallBtn, a4Btn);

                Optional<ButtonType> result = printAlert.showAndWait();
                if (result.isPresent()) {
                    try {
                        byte[] pdfBytes;
                        if (result.get() == smallBtn) {
                            pdfBytes = orderBO.generateOrderBill80mm(orderId);
                        } else {
                            pdfBytes = orderBO.generateOrderBillA4(orderId);
                        }

                        if (pdfBytes != null) {
                            File tempFile = File.createTempFile("Invoice_" + orderId, ".pdf");
                            Files.write(tempFile.toPath(), pdfBytes);
                            Desktop.getDesktop().open(tempFile);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        AlertUtil.showAlert(Alert.AlertType.ERROR, "Error generating bill: " + ex.getMessage());
                    }
                }

                resetPosForm();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invoice Placement Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void resetPosForm() {
        obList.clear();
        tblOrderCart.refresh();

        lblDate.setText(String.valueOf(LocalDate.now()));

        cmbCustomerId.getItems().clear();
        cmbCustomerId.getSelectionModel().clearSelection();
        loadCustomerIds();
        selectedCustomer = null;

        cmbProductId.getItems().clear();
        cmbProductId.getSelectionModel().clearSelection();
        loadProductIds();
        selectedProduct = null;
        txtUnitPrice.clear();
        lblQtyOnHand.setText("");
        txtQty.clear();

        loadTax();

        txtDiscount.setText("0");
        txtAmount.setText("-");
        lblBalance.setText("0.00");
        lblNetTotal.setText("0.00");
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
        if (txtTax.isEditable()) {
            try {
                double taxValue = Double.parseDouble(txtTax.getText());
                TaxDto taxDto = new TaxDto(1, taxValue);
                taxBO.updateTax(taxDto);
                loadTax();
                txtTax.setEditable(false);
                btnUpdate.setText("Edit");
                btnUpdate.setStyle("-fx-background-color: #b1b1b1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Tax updated!");
            } catch (Exception e) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid tax value!");
            }
        } else {
            txtTax.setEditable(true);
            btnUpdate.setText("Save");
            btnUpdate.setStyle("-fx-background-color: #3a4b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
        }
    }

    @FXML
    void btnPreviousOrders(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/OrderManage.fxml"));
            body.getChildren().clear();
            body.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
