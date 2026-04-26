package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
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

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
    private Label lblNetTotal;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private Label lblDate;

    @FXML
    private TableView<CartTM> tblOrderCart;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtDiscount;

    @FXML
    private TextField txtAmount;

    @FXML
    private Label lblBalance;

    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);

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
        setOrderId();
        loadCustomerIds();
        loadProductIds();
        txtDiscount.setText("0");
        txtAmount.setText("");
        
        txtDiscount.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateDiscount();
            }
        });
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
        for (CustomerDto dto : currentCustomers) {
            cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
        }
    }

    private void loadProductIds() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProductId.getItems().add(dto.getName());
        }
    }

    @FXML
    void btnNewCustomerOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Customer");
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
            stage.setTitle("New Product");
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
        }
    }

    @FXML
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProductId.getValue();
        if (selection != null && currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                if (dto.getName().equals(selection)) {
                    selectedProduct = dto;
                    lblUnitPrice.setText("Rs. " + dto.getPrice() + "/=");
                    lblQtyOnHand.setText(dto.getQty() + " Kg");
                    break;
                }
            }
        }
    }

    @FXML
    void cmbProductOnKeyReleased(javafx.scene.input.KeyEvent event) {
        String searchText = cmbProductId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                cmbProductId.getItems().add(dto.getName());
            }
        } else {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                if (dto.getName().toLowerCase().contains(searchText)) {
                    cmbProductId.getItems().add(dto.getName());
                }
            }
            cmbProductId.show();
        }
    }

    @FXML
    void btnAddToCartOnAction(ActionEvent event) {
        if (selectedProduct == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a product first!").show();
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
        try {
            String priceStr = lblUnitPrice.getText().replace("Rs. ", "").replace("/=", "");
            double unitPrice = Double.parseDouble(priceStr.trim());
            double total = qty * unitPrice;
            Button btn = new Button("Remove");

            btn.setOnAction((e) -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to remove?", yes, no).showAndWait();

                if (result.orElse(no) == yes) {
                    int index = tblOrderCart.getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        obList.remove(index);
                        calculateTotal();
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
        double finalTotal = netTotal - discountAmount;
        lblNetTotal.setText(String.format("%.2f", finalTotal));
        
        calculateBalance();
    }

    private void calculateBalance() {
        try {
            double netTotal = Double.parseDouble(lblNetTotal.getText());
            double amount = 0.0;
            
            if (txtAmount.getText() != null && !txtAmount.getText().isEmpty()) {
                amount = Double.parseDouble(txtAmount.getText());
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
        
        try {
            double netTotal = Double.parseDouble(lblNetTotal.getText());
            double amount = 0.0;
            double discount = 0.0;
            
            if (txtAmount.getText() != null && !txtAmount.getText().isEmpty()) {
                amount = Double.parseDouble(txtAmount.getText());
            }
            if (txtDiscount.getText() != null && !txtDiscount.getText().isEmpty()) {
                discount = Double.parseDouble(txtDiscount.getText());
            }
            
            double totalAfterDiscount = netTotal - discount;
            if (amount < totalAfterDiscount) {
                new Alert(Alert.AlertType.ERROR, "Insufficient Amount! Total: " + totalAfterDiscount + ", Paid: " + amount).show();
                return;
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Please enter valid amount!").show();
            return;
        }

        String orderId = lblOrderId.getText();
        LocalDate date = LocalDate.parse(lblDate.getText());
        CustomerDto customerDto = selectedCustomer;

        ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
        for (CartTM tm : obList) {
            ProductDto productDto = productBO.searchProduct(tm.getCode());
            details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
        }

        OrderDto orderDto = new OrderDto(orderId, date, customerDto, details);
        try {
            boolean isPlaced = orderBO.placeOrder(orderDto);
            if (isPlaced) {
                new Alert(Alert.AlertType.INFORMATION, "Order Placed Successfully!").show();
                obList.clear();
                calculateTotal();
                setOrderId();
                clearProductFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Order Placement Failed!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    private void clearProductFields() {
        cmbProductId.getSelectionModel().clearSelection();
        selectedProduct = null;
        lblUnitPrice.setText("");
        lblQtyOnHand.setText("");
        txtQty.clear();
    }

    @FXML
    void btnUpdate(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Update feature coming soon!").show();
    }

    @FXML
    void btnPreviousOrders(ActionEvent event) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("/view/OrderManage.fxml"));
            body.getChildren().clear();
            body.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
