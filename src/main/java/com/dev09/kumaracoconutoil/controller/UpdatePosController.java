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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdatePosController implements Initializable {

    private ArrayList<CustomerDto> currentCustomers;

    @FXML
    private AnchorPane body;

    @FXML
    private Button btnUpdate;

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
    private Label lblBalance;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private TableView<CartTM> tblOrderCart;

    @FXML
    private TextField txtAmount;

    @FXML
    private TextField txtDiscount;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtTax;

    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private CustomerDto selectedCustomer;
    private ProductDto selectedProduct;
    private ArrayList<ProductDto> currentProducts;
    private Timeline productResetTimer;
    private Timeline customerResetTimer;
    private OrderDto originalOrderDto;
    private java.util.Map<String, Double> removedQtyMap = new java.util.HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblOrderCart.setItems(obList);
        loadCustomerIds();
        loadProductIds();
    }

    private void loadCustomerIds() {
        try {
            currentCustomers = customerBO.getAllCustomers();
            for (CustomerDto dto : currentCustomers) {
                cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductIds() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProductId.getItems().add(dto.getName());
        }
    }
    
    @FXML
    void cmbCustomerOnAction(ActionEvent event) {
        String selection = cmbCustomerId.getValue();
        if (selection != null && currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                String display = dto.getName() + " - " + dto.getContact();
                if (display.equals(selection)) {
                    selectedCustomer = dto;
                    break;
                }
            }
        }
    }

    @FXML
    void cmbCustomerOnKeyReleased(KeyEvent event) {
        String searchText = cmbCustomerId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbCustomerId.getItems().clear();
            for (CustomerDto dto : currentCustomers) {
                cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
            }
            if (customerResetTimer != null) customerResetTimer.stop();
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

            if (customerResetTimer != null) customerResetTimer.stop();
            customerResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbCustomerId.getItems().isEmpty()) {
                    cmbCustomerId.getEditor().setText("");
                    cmbCustomerId.getItems().clear();
                    for (CustomerDto dto : currentCustomers) {
                        cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
                    }
                }
            }));
            customerResetTimer.setCycleCount(1);
            customerResetTimer.play();
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
    void cmbProductOnKeyReleased(KeyEvent event) {
        String searchText = cmbProductId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                cmbProductId.getItems().add(dto.getName());
            }
            if (productResetTimer != null) productResetTimer.stop();
        } else {
            cmbProductId.getItems().clear();
            for (ProductDto dto : currentProducts) {
                if (dto.getName().toLowerCase().contains(searchText)) {
                    cmbProductId.getItems().add(dto.getName());
                }
            }
            cmbProductId.show();

            if (productResetTimer != null) productResetTimer.stop();
            productResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProductId.getItems().isEmpty()) {
                    cmbProductId.getEditor().setText("");
                    cmbProductId.getItems().clear();
                    for (ProductDto dto : currentProducts) {
                        cmbProductId.getItems().add(dto.getName());
                    }
                }
            }));
            productResetTimer.setCycleCount(1);
            productResetTimer.play();
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
            String priceStr = lblUnitPrice.getText().replace("Rs. ", "").replace("/=", "");
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
                            String prodCode = obList.get(i).getCode();
                            double removedQty = obList.get(i).getQty();
                            removedQtyMap.put(prodCode, removedQtyMap.getOrDefault(prodCode, 0.0) + removedQty);
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
            if (discountText != null && !discountText.trim().isEmpty()) {
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
            if (taxText != null && !taxText.trim().isEmpty()) {
                taxPercent = Double.parseDouble(taxText.trim());
            }
        } catch (NumberFormatException e) {
            taxPercent = 0.0;
        }

        double taxAmount = totalAfterDiscount * (taxPercent / 100.0);
        double finalTotal = totalAfterDiscount + taxAmount;

        lblNetTotal.setText(String.format("%.2f", finalTotal));
        calculateBalance();
    }

    public void loadOrderForEdit(OrderDto orderDto) {
        this.originalOrderDto = orderDto;
        this.removedQtyMap.clear();
        try {
            obList.clear();
            lblOrderId.setText(orderDto.getOrderId());
            lblDate.setText(String.valueOf(orderDto.getDate()));

            if (orderDto.getCustomer() != null) {
                cmbCustomerId.getItems().clear();
                cmbCustomerId.getItems().add(orderDto.getCustomer().getName() + " - " + orderDto.getCustomer().getContact());
                cmbCustomerId.getSelectionModel().select(0);
                selectedCustomer = orderDto.getCustomer();
            }

            txtDiscount.setText(orderDto.getDiscount() > 0 ? String.valueOf(orderDto.getDiscount()) : "");
            txtTax.setText(orderDto.getTax() > 0 ? String.valueOf(orderDto.getTax()) : "");
            txtAmount.setText("");
            
            if (orderDto.getOrderProducts() != null) {
                double cartTotal = 0;
                for (OrderProductDetailsDto detail : orderDto.getOrderProducts()) {
                    if (detail.getProduct() != null) {
                        ProductDto product = detail.getProduct();
                        double qty = detail.getQuantity();
                        double unitPrice = detail.getUnitPrice();
                        double total = qty * unitPrice;
                        cartTotal += total;

                        Button btn = new Button("Remove");
                        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        btn.setOnAction(e -> {
                            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to remove?", yes, no).showAndWait();
                            if (result.orElse(no) == yes) {
                                String prodCode = product.getId();
                                double removedQty = 0;
                                for (CartTM item : obList) {
                                    if (item.getCode().equals(prodCode)) {
                                        removedQty = item.getQty();
                                        break;
                                    }
                                }
                                removedQtyMap.put(prodCode, removedQtyMap.getOrDefault(prodCode, 0.0) + removedQty);
                                obList.removeIf(item -> item.getCode().equals(prodCode));
                                calculateTotal();
                            }
                        });

                        obList.add(new CartTM(product.getId(), product.getName(), qty, unitPrice, total, btn));
                    }
                }
                lblNetTotal.setText(String.format("%.2f", cartTotal));
            }
            
            lblBalance.setText("0.00/");
            tblOrderCart.refresh();
            calculateTotal();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading order: " + e.getMessage()).show();
        }
    }

    private void calculateBalance() {
        try {
            double netTotal = Double.parseDouble(lblNetTotal.getText());
            double amount = 0;
            String amountText = txtAmount.getText();
            if (amountText != null && !amountText.trim().isEmpty()) {
                amount = Double.parseDouble(amountText.trim());
            }
            
            if (amount < netTotal) {
                lblBalance.setText("Insufficient/");
                lblBalance.setStyle("-fx-text-fill: #e74c3c;");
            } else {
                double balance = amount - netTotal;
                lblBalance.setText(String.format("%.2f/", balance));
                lblBalance.setStyle("-fx-text-fill: #00a118;");
            }
        } catch (NumberFormatException e) {
            lblBalance.setText("0.00/");
        }
    }

    private void validateDiscount() {
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
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage()).show();
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
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnUpdateOnAction(ActionEvent event) {
    }

    @FXML
    void btnUpdateOrderOnAction(ActionEvent event) {
        if (selectedCustomer == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a customer!").show();
            return;
        }
        
        if (obList.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please add inventory items to the cart!").show();
            return;
        }
        
        try {
            double netTotal = 0.0;
            for (CartTM tm : obList) {
                netTotal += tm.getTotal();
            }
            
            double discountPercent = 0.0;
            try {
                String discountText = txtDiscount.getText();
                if (discountText != null && !discountText.trim().isEmpty()) {
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
                if (taxText != null && !taxText.trim().isEmpty()) {
                    taxPercent = Double.parseDouble(taxText.trim());
                }
            } catch (NumberFormatException e) {
                taxPercent = 0.0;
            }
            
            double taxAmount = totalAfterDiscount * (taxPercent / 100.0);
            double finalTotal = totalAfterDiscount + taxAmount;
            
            double amountPaid = 0;
            try {
                String amountText = txtAmount.getText();
                if (amountText != null && !amountText.trim().isEmpty()) {
                    amountPaid = Double.parseDouble(amountText.trim());
                }
            } catch (NumberFormatException e) {
                amountPaid = 0;
            }
            
            double balance = amountPaid - finalTotal;
            if (balance < 0) {
                new Alert(Alert.AlertType.WARNING, "Amount paid must be at least the total amount!").show();
                return;
            }

            java.util.Map<String, Double> originalCartQty = new java.util.HashMap<>();
            if (originalOrderDto != null && originalOrderDto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : originalOrderDto.getOrderProducts()) {
                    if (detail.getProduct() != null) {
                        originalCartQty.put(detail.getProduct().getId(), detail.getQuantity());
                    }
                }
            }

            java.util.Map<String, Double> newCartQty = new java.util.HashMap<>();
            for (CartTM tm : obList) {
                newCartQty.put(tm.getCode(), tm.getQty());
            }

            for (String prodId : originalCartQty.keySet()) {
                double originalQty = originalCartQty.get(prodId);
                double currentQty = newCartQty.getOrDefault(prodId, 0.0);
                double difference = originalQty - currentQty;
                if (difference > 0) {
                    ProductDto prod = productBO.searchProduct(prodId);
                    if (prod != null) {
                        prod.setQty(prod.getQty() + difference);
                        productBO.updateProduct(prod);
                    }
                } else if (difference < 0) {
                    productBO.updateStock(prodId, Math.abs(difference));
                }
            }

            for (String prodId : newCartQty.keySet()) {
                if (!originalCartQty.containsKey(prodId)) {
                    productBO.updateStock(prodId, newCartQty.get(prodId));
                }
            }
            
            ArrayList<OrderProductDetailsDto> orderProducts = new ArrayList<>();
            for (CartTM tm : obList) {
                ProductDto product = new ProductDto();
                product.setId(tm.getCode());
                product.setName(tm.getDescription());
                
                OrderProductDetailsDto detail = new OrderProductDetailsDto();
                detail.setProduct(product);
                detail.setQuantity(tm.getQty());
                detail.setUnitPrice(tm.getUnitPrice());
                orderProducts.add(detail);
            }
            
            OrderDto orderDto = new OrderDto();
            orderDto.setOrderId(lblOrderId.getText());
            orderDto.setDate(LocalDate.parse(lblDate.getText()));
            orderDto.setCustomer(selectedCustomer);
            orderDto.setOrderProducts(orderProducts);
            orderDto.setAmountPaid(amountPaid);
            orderDto.setDiscount(discountPercent);
            orderDto.setTax(taxPercent);
            orderDto.setBalance(balance);
            
            boolean success = orderBO.updateOrder(orderDto);
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Invoice updated successfully!").show();
                
                Stage stage = (Stage) body.getScene().getWindow();
                stage.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to update invoice!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error updating order: " + e.getMessage()).show();
        }
    }

    @FXML
    void txtDiscountOnAction(ActionEvent event) {
    }

    @FXML
    void txtDiscountOnKeyReleased(KeyEvent event) {
        calculateTotal();
    }

    @FXML
    void txtAmountOnKeyReleased(KeyEvent event) {
        calculateBalance();
    }

}
