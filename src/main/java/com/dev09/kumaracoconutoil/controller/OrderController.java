package com.dev09.kumaracoconutoil.controller;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

    @FXML
    private AnchorPane body;

    @FXML
    private AnchorPane posContent;

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
    private Label lblOrderId;

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
    private boolean isContentLoading = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isContentLoading) return;
        isContentLoading = true;
        loadPosContent();
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblOrderCart.setItems(obList);

        lblDate.setText(String.valueOf(LocalDate.now()));
        setOrderId();
        loadCustomerIds();
        loadProductIds();
        loadTax();
        
        txtDiscount.setText("0");
        txtAmount.setText("-");
        
        txtDiscount.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateDiscount();
            }
        });
    }

    private void loadPosContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pos_content.fxml"));
            loader.setController(this);
            isContentLoading = true;
            javafx.scene.Parent root = loader.load();
            posContent.getChildren().clear();
            posContent.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isContentLoading = false;
        }
    }
    
    private void loadTax() {
        try {
            TaxDto taxDto = taxBO.searchTax("1");
            if (taxDto != null) {
                txtTax.setText(String.valueOf(taxDto.getTax()));
            } else {
                txtTax.setText("0");
            }
        } catch (Exception e) {
            txtTax.setText("0");
        }
    }
    
    public void loadOrderForEdit(OrderDto orderDto) {
        try {
            lblOrderId.setText(orderDto.getOrderId());
            lblDate.setText(String.valueOf(orderDto.getDate()));
            
            if (orderDto.getCustomer() != null) {
                cmbCustomerId.getItems().clear();
                cmbCustomerId.getItems().add(orderDto.getCustomer().getName());
                cmbCustomerId.getSelectionModel().select(0);
                selectedCustomer = orderDto.getCustomer();
            }
            
            txtDiscount.setText(String.valueOf(orderDto.getDiscount()));
            txtTax.setText(String.valueOf(orderDto.getTax()));
            
            if (orderDto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : orderDto.getOrderProducts()) {
                    ProductDto product = detail.getProduct();
                    if (product != null) {
                        double qty = detail.getQuantity();
                        double unitPrice = detail.getUnitPrice();
                        double total = qty * unitPrice;
                        
                        Button btn = new Button("Remove");
                        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        btn.setOnAction(e -> {
                            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to remove?", yes, no).showAndWait();
                            if (result.orElse(no) == yes) {
                                obList.removeIf(item -> item.getCode().equals(product.getId()));
                                calculateTotal();
                            }
                        });
                        
                        obList.add(new CartTM(product.getId(), product.getName(), qty, unitPrice, total, btn));
                    }
                }
                calculateTotal();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading order: " + e.getMessage()).show();
        }
    }
    
    private void validateDiscount() {
        String text = txtDiscount.getText();
        if (text != null && !text.isEmpty()) {
            try {
                int val = Integer.parseInt(text);
                if (val > 100) {
                    txtDiscount.setText("100");
                } else if (val < 0) {
                    txtDiscount.setText("0");
                }
            } catch (NumberFormatException e) {
                txtDiscount.setText("0");
            }
        } else {
            txtDiscount.setText("0");
        }
    }

    private void setOrderId() {
        try {
            lblOrderId.setText(orderBO.generateNextOrderId());
        } catch (Exception e) {
            e.printStackTrace();
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
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Customer");
            stage.setResizable(false);
            stage.showAndWait();
            
            cmbCustomerId.getItems().clear();
            loadCustomerIds();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnNewProductOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/inventory.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Inventory");
            stage.setResizable(false);
            stage.showAndWait();
            
            cmbProductId.getItems().clear();
            loadProductIds();
        } catch (java.io.IOException e) {
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
            new Alert(Alert.AlertType.WARNING, "Please select an inventory first!").show();
            return;
        }
        String code = selectedProduct.getId();
        String description = selectedProduct.getName();
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Please enter a valid quantity!").show();
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
            new Alert(Alert.AlertType.WARNING, "Total quantity exceeds available stock!\nAvailable: " + qtyOnHand + " Kg\nIn Cart: " + existingQtyInCart + " Kg").show();
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
                    qty += obList.get(i).getQty();
                    total = unitPrice * qty;

                    obList.get(i).setQty(qty);
                    obList.get(i).setTotal(total);

                    tblOrderCart.refresh();
                    calculateTotal();
                    txtQty.clear();
                    return;
                }
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
                discountText = discountText.replace("%", "").trim();
                discountPercent = Double.parseDouble(discountText);
                if (discountPercent < 0 || discountPercent > 100) {
                    new Alert(Alert.AlertType.WARNING, "Discount must be between 0% and 100%!").show();
                    txtDiscount.setText("0%");
                    discountPercent = 0.0;
                }
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
            double amount = 0.0;
            String amountText = txtAmount.getText();
            
            if (amountText != null && !amountText.isEmpty() && !amountText.equals("-")) {
                amount = Double.parseDouble(amountText);
            } else {
                lblBalance.setText("0.00");
                lblBalance.setStyle("-fx-text-fill: #00a118;");
                return;
            }
            
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
        validateDiscount();
        calculateTotal();
    }

    @FXML
    void btnPlaceOrderOnAction(ActionEvent event) {
        if (selectedCustomer == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a customer first!").show();
            return;
        }
        if (obList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Cart is empty!").show();
            return;
        }

        String orderId = lblOrderId.getText();
        LocalDate date = LocalDate.parse(lblDate.getText());
        CustomerDto customerDto = selectedCustomer;

        double netTotal = 0.0;
        double discount = 0.0;
        double taxRate = 0.0;
        double taxAmount = 0.0;
        double balance = 0.0;

        try {
            netTotal = Double.parseDouble(lblNetTotal.getText());
            discount = Double.parseDouble(txtDiscount.getText());
            taxRate = Double.parseDouble(txtTax.getText()); // This is the TAX RATE (e.g., 8.0)
            taxAmount = netTotal * (taxRate / 100.0); // Calculate TAX AMOUNT correctly
            balance = Double.parseDouble(lblBalance.getText());
        } catch (NumberFormatException e) {
        }

        double amount = netTotal;

        ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
        for (CartTM tm : obList) {
            ProductDto productDto = productBO.searchProduct(tm.getCode());
            details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
        }

        OrderDto orderDto = new OrderDto(orderId, date, customerDto, details, amount, discount, taxAmount, taxRate, balance);
        try {
            boolean isPlaced = orderBO.placeOrder(orderDto);
            if (isPlaced) {
                new Alert(Alert.AlertType.INFORMATION, "Invoice Placed Successfully!").show();
                
                // Ask user how to print the bill
                Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
                printAlert.setTitle("Print Bill");
                printAlert.setHeaderText("How do you need to print the bill?");
                printAlert.setContentText("Choose the print format:");
                
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
                        new Alert(Alert.AlertType.ERROR, "Error generating bill: " + ex.getMessage()).show();
                    }
                }

                resetPosForm();
            } else {
                new Alert(Alert.AlertType.ERROR, "Invoice Placement Failed!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    private void clearProductFields() {
        cmbProductId.getSelectionModel().clearSelection();
        selectedProduct = null;
        txtUnitPrice.clear();
        lblQtyOnHand.setText("");
        txtQty.clear();
    }

    private void resetPosForm() {
        obList.clear();
        tblOrderCart.refresh();
        
        setOrderId();
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
                btnUpdate.setStyle("-fx-background-color: #b1b1b1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                new Alert(Alert.AlertType.INFORMATION, "Tax updated successfully!").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Invalid tax value!").show();
            }
        } else {
            txtTax.setEditable(true);
            btnUpdate.setText("Save");
            btnUpdate.setStyle("-fx-background-color: #3a4b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        }
    }

    @FXML
    void btnPreviousOrders(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/OrderManage.fxml"));
            body.getChildren().clear();
            body.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
