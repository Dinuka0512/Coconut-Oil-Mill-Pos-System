package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
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
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class SimplePOSController implements Initializable {

    @FXML
    private ComboBox<String> cmbProduct;

    @FXML
    private ComboBox<String> cmbCustomer;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtUnitPriceDisplay;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblDueWarning;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblItemCode;

    @FXML
    private TableView<CartTM> tblCart;

    @FXML
    private TableColumn<CartTM, Integer> colIndex;

    @FXML
    private TableColumn<CartTM, String> colProduct;

    @FXML
    private TableColumn<CartTM, Double> colQty;

    @FXML
    private TableColumn<CartTM, Double> colPrice;

    @FXML
    private TableColumn<CartTM, Double> colTotal;

    @FXML
    private TableColumn<CartTM, Button> colAction;

    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);

    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private ArrayList<ProductDto> currentProducts;
    private ArrayList<CustomerDto> currentCustomers;
    private ProductDto selectedProduct;
    private CustomerDto selectedCustomer;
    private Timeline productResetTimer;
    private Timeline customerResetTimer;

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
        colProduct.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblCart.setItems(obList);
        loadProducts();
        loadCustomers();

        txtUnitPriceDisplay.setEditable(true);

        txtQty.setOnAction(e -> {
            if (selectedProduct == null) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
                cmbProduct.requestFocus();
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
            txtUnitPriceDisplay.requestFocus();
        });

        txtUnitPriceDisplay.setOnAction(e -> btnAddToCartOnAction(null));

        cmbProduct.requestFocus();

        javafx.application.Platform.runLater(() -> {
            javafx.scene.Scene scene = tblCart.getScene();
            if (scene != null) {
                scene.getAccelerators().put(
                    new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.ENTER, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                    () -> btnPlaceOrderOnAction(null)
                );
            }
        });
    }

    private void loadProducts() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
        }
    }

    private void loadCustomers() {
        currentCustomers = customerBO.getAllCustomers();
        int index = 0;
        for (CustomerDto dto : currentCustomers) {
            cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
            if ("CUST001".equals(dto.getCustId())) {
                selectedCustomer = dto;
                cmbCustomer.getSelectionModel().select(index);
            }
            index++;
        }
        checkCustomerDue();
    }

    @FXML
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProduct.getValue();
        if (selection != null && currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (display.equals(selection) || dto.getName().equals(selection)) {
                    selectedProduct = dto;
                    txtUnitPriceDisplay.setText(String.valueOf(dto.getPrice()));
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
        String searchText = cmbProduct.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbProduct.getItems().clear();
            for (ProductDto dto : currentProducts) {
                cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
            if (productResetTimer != null) productResetTimer.stop();
        } else {
            cmbProduct.getItems().clear();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (dto.getName().toLowerCase().contains(searchText) ||
                    (dto.getItemcode() != null ? dto.getItemcode() : "").toLowerCase().contains(searchText)) {
                    cmbProduct.getItems().add(display);
                }
            }
            cmbProduct.show();

            if (productResetTimer != null) productResetTimer.stop();
            productResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProduct.getItems().isEmpty()) {
                    cmbProduct.getEditor().setText("");
                    cmbProduct.getItems().clear();
                    for (ProductDto dto : currentProducts) {
                        cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
                    }
                }
            }));
            productResetTimer.setCycleCount(1);
            productResetTimer.play();
        }
    }

    @FXML
    void cmbCustomerOnAction(ActionEvent event) {
        String selection = cmbCustomer.getValue();
        if (selection != null && currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                if ((dto.getName() + " - " + dto.getContact()).equals(selection)) {
                    selectedCustomer = dto;
                    checkCustomerDue();
                    break;
                }
            }
        }
    }

    private void checkCustomerDue() {
        if (selectedCustomer != null) {
            double due = selectedCustomer.getDueAmount();
            if (due > 0) {
                lblDueWarning.setText("⚠ Due: Rs. " + String.format("%.2f", due));
                lblDueWarning.setVisible(true);
            } else {
                lblDueWarning.setVisible(false);
            }
        }
    }

    @FXML
    void cmbCustomerOnKeyReleased(KeyEvent event) {
        String searchText = cmbCustomer.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            cmbCustomer.getItems().clear();
            for (CustomerDto dto : currentCustomers) {
                cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
            }
            if (customerResetTimer != null) customerResetTimer.stop();
        } else {
            cmbCustomer.getItems().clear();
            for (CustomerDto dto : currentCustomers) {
                String display = dto.getName() + " - " + dto.getContact();
                if (dto.getName().toLowerCase().contains(searchText) ||
                    dto.getContact().toLowerCase().contains(searchText)) {
                    cmbCustomer.getItems().add(display);
                }
            }
            cmbCustomer.show();

            if (customerResetTimer != null) customerResetTimer.stop();
            customerResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbCustomer.getItems().isEmpty()) {
                    cmbCustomer.getEditor().setText("");
                    cmbCustomer.getItems().clear();
                    for (CustomerDto dto : currentCustomers) {
                        cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
                    }
                }
            }));
            customerResetTimer.setCycleCount(1);
            customerResetTimer.play();
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

            cmbProduct.getItems().clear();
            loadProducts();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage());
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

            cmbCustomer.getItems().clear();
            loadCustomers();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage());
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
            double unitPrice = Double.parseDouble(txtUnitPriceDisplay.getText().trim());
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
                    obList.removeIf(item -> item.getBtn() == btn);
                    calculateTotal();
                }
            });

            boolean merged = false;
            for (CartTM item : obList) {
                if (code.equals(item.getCode()) && unitPrice == item.getUnitPrice()) {
                    item.setQty(item.getQty() + qty);
                    item.setTotal(item.getTotal() + total);
                    tblCart.refresh();
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                obList.add(new CartTM(code, description, qty, unitPrice, total, btn));
            }
            calculateTotal();
            clearProductSelection();
            cmbProduct.requestFocus();
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid unit price!");
        }
    }

    private void clearProductSelection() {
        selectedProduct = null;
        txtQty.clear();
        txtQty.setText("1");
        txtUnitPriceDisplay.clear();
        lblQtyOnHand.setText("0Kg");
        lblItemName.setText("-");
        lblItemCode.setText("-");
        cmbProduct.getEditor().clear();
        cmbProduct.getSelectionModel().clearSelection();
        cmbProduct.getItems().clear();
        loadProducts();
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }
        lblNetTotal.setText(String.format("Total: Rs. %.2f", netTotal));
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

        try {
            String totalText = lblNetTotal.getText().replace("Total: Rs. ", "").trim();
            double netTotal = Double.parseDouble(totalText);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/balance.fxml"));
            Parent root = loader.load();
            BalanceController balanceController = loader.getController();

            balanceController.setOrderData(obList, selectedCustomer, netTotal);

            Stage balanceStage = new Stage();
            balanceStage.setScene(new Scene(root));
            balanceStage.setTitle("Payment / Balance");
            balanceStage.setResizable(false);
            balanceStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            balanceStage.showAndWait();

            if (balanceController.isOrderPlaced()) {
                Stage stage = (Stage) tblCart.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }
}
