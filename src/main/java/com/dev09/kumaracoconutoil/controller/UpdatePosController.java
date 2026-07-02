package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
    private ComboBox<String> cmbCustomerId;

    @FXML
    private ComboBox<String> cmbProductId;

    @FXML
    private TableColumn<CartTM, Integer> colIndex;

    @FXML
    private TableColumn<CartTM, String> colDescription;

    @FXML
    private TableColumn<CartTM, Double> colQty;

    @FXML
    private TableColumn<CartTM, Double> colUnitPrice;

    @FXML
    private TableColumn<CartTM, Double> colTotal;

    @FXML
    private TableColumn<CartTM, Button> colAction;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblItemCode;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private TableView<CartTM> tblOrderCart;

    @FXML
    private TextField txtQty;

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
        colIndex.setCellFactory(col -> new TableCell<CartTM, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblOrderCart.setItems(obList);
        loadCustomerIds();
        loadProductIds();

        txtQty.setOnAction(e -> {
            if (selectedProduct == null) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
                cmbProductId.requestFocus();
                return;
            }
            try {
                double qty = Double.parseDouble(txtQty.getText());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter a valid quantity!");
                txtQty.requestFocus();
                return;
            }
            txtUnitPrice.requestFocus();
        });

        txtUnitPrice.setOnAction(e -> btnAddToCartOnAction(null));

        Platform.runLater(() -> {
            Scene scene = tblOrderCart.getScene();
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
                    () -> btnUpdateOrderOnAction(null)
                );
            }
        });
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
            cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
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
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (display.equals(selection) || dto.getName().equals(selection)) {
                    selectedProduct = dto;
                    txtUnitPrice.setText(String.valueOf(dto.getPrice()));
                    lblQtyOnHand.setText(dto.getQty() + " Kg");
                    lblItemName.setText(dto.getName());
                    lblItemCode.setText(dto.getItemcode() != null ? dto.getItemcode() : "-");
                    txtQty.requestFocus();
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
                cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
            if (productResetTimer != null) productResetTimer.stop();
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

            if (productResetTimer != null) productResetTimer.stop();
            productResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProductId.getItems().isEmpty()) {
                    cmbProductId.getEditor().setText("");
                    cmbProductId.getItems().clear();
                    for (ProductDto dto : currentProducts) {
                        cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
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

        if (qty <= 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Quantity must be greater than zero!");
            return;
        }

        double qtyOnHand = selectedProduct.getQty();
        double existingQtyInCart = 0;
        for (CartTM item : obList) {
            if (code.equals(item.getCode())) {
                existingQtyInCart = item.getQty();
                break;
            }
        }

        double totalQtyInCart = existingQtyInCart + qty;
        if (totalQtyInCart > qtyOnHand) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Total quantity exceeds available stock!\nAvailable: " + qtyOnHand + " Kg\nIn Cart: " + existingQtyInCart + " Kg");
            return;
        }

        try {
            double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());
            if (unitPrice <= 0) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Unit price must be greater than zero!");
                return;
            }
            double total = qty * unitPrice;
            Button btn = new Button("X");
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");

            btn.setOnAction(e -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Remove this item?", yes, no).showAndWait();
                if (result.orElse(no) == yes) {
                    String prodCode = null;
                    double removedQty = 0;
                    for (int i = 0; i < obList.size(); i++) {
                        if (obList.get(i).getBtn() == btn) {
                            prodCode = obList.get(i).getCode();
                            removedQty = obList.get(i).getQty();
                            break;
                        }
                    }
                    if (prodCode != null) {
                        removedQtyMap.put(prodCode, removedQtyMap.getOrDefault(prodCode, 0.0) + removedQty);
                    }
                    obList.removeIf(item -> item.getBtn() == btn);
                    calculateTotal();
                }
            });

            boolean merged = false;
            for (CartTM item : obList) {
                if (code.equals(item.getCode()) && unitPrice == item.getUnitPrice()) {
                    item.setQty(item.getQty() + qty);
                    item.setTotal(item.getTotal() + total);
                    tblOrderCart.refresh();
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                obList.add(new CartTM(code, description, qty, unitPrice, total, btn));
            }
            calculateTotal();
            clearProductSelection();
            cmbProductId.requestFocus();
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid unit price!");
        }
    }

    private void clearProductSelection() {
        selectedProduct = null;
        txtQty.clear();
        txtQty.setText("1");
        txtUnitPrice.clear();
        lblQtyOnHand.setText("0Kg");
        lblItemName.setText("-");
        lblItemCode.setText("-");
        cmbProductId.getEditor().clear();
        cmbProductId.getSelectionModel().clearSelection();
        cmbProductId.getItems().clear();
        loadProductIds();
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }
        lblNetTotal.setText(String.format("Total: Rs. %.2f", netTotal));
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

            if (orderDto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : orderDto.getOrderProducts()) {
                    if (detail.getProduct() != null) {
                        ProductDto product = detail.getProduct();
                        double qty = detail.getQuantity();
                        double unitPrice = detail.getUnitPrice();
                        double total = qty * unitPrice;

                        Button btn = new Button("X");
                        btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                        btn.setOnAction(e -> {
                            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Remove this item?", yes, no).showAndWait();
                            if (result.orElse(no) == yes) {
                                String prodCode = null;
                                double removedQty = 0;
                                for (int i = 0; i < obList.size(); i++) {
                                    if (obList.get(i).getBtn() == btn) {
                                        prodCode = obList.get(i).getCode();
                                        removedQty = obList.get(i).getQty();
                                        break;
                                    }
                                }
                                if (prodCode != null) {
                                    removedQtyMap.put(prodCode, removedQtyMap.getOrDefault(prodCode, 0.0) + removedQty);
                                }
                                obList.removeIf(item -> item.getBtn() == btn);
                                calculateTotal();
                            }
                        });

                        obList.add(new CartTM(product.getId(), product.getName(), qty, unitPrice, total, btn));
                    }
                }
            }

            tblOrderCart.refresh();
            calculateTotal();

            Platform.runLater(() -> cmbProductId.requestFocus());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading order: " + e.getMessage());
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage());
        }
    }

    @FXML
    void btnUpdateOrderOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can update invoices!");
            return;
        }
        if (selectedCustomer == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a customer!");
            return;
        }

        if (obList.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please add inventory items to the cart!");
            return;
        }

        try {
            double netTotal = 0.0;
            for (CartTM tm : obList) {
                netTotal += tm.getTotal();
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
            orderDto.setAmountPaid(netTotal);
            orderDto.setDiscount(0);
            orderDto.setTax(0);
            orderDto.setBalance(0);

            boolean success = orderBO.updateOrder(orderDto);
            if (success) {
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Invoice updated successfully!");

                Stage stage = (Stage) body.getScene().getWindow();
                stage.close();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update invoice!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error updating order: " + e.getMessage());
        }
    }

}
