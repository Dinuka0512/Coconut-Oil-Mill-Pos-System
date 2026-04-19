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
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderController implements Initializable {

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
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProductId.getValue();
        if (selection != null && currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                if (dto.getName().equals(selection)) {
                    selectedProduct = dto;
                    lblUnitPrice.setText(String.valueOf(dto.getPrice()));
                    lblQtyOnHand.setText(String.valueOf(dto.getQty()));
                    break;
                }
            }
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
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText());
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Please enter a valid quantity!").show();
            return;
        }
        double unitPrice = Double.parseDouble(lblUnitPrice.getText());
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
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }
        lblNetTotal.setText(String.valueOf(netTotal));
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
}
