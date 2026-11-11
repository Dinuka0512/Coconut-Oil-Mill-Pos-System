package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.ComboBoxUtil;
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

        ComboBoxUtil.enableArrowSelection(cmbProduct);
        ComboBoxUtil.enableArrowSelection(cmbCustomer);

        cmbProduct.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbProduct.getEditor() != null && cmbProduct.getEditor().getText().trim().isEmpty()) {
                repopulateAllProducts();
            }
        });
        cmbCustomer.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbCustomer.getEditor() != null && cmbCustomer.getEditor().getText().trim().isEmpty()) {
                repopulateAllCustomers();
            }
        });

        javafx.application.Platform.runLater(() -> {
            if (cmbProduct.getEditor() != null) {
                cmbProduct.getEditor().requestFocus();
            } else {
                cmbProduct.requestFocus();
            }
        });

        installFocusFlowHandlers();

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
            if (AppConstants.DEMO_CUSTOMER_ID.equals(dto.getCustId())) {
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
        if ((selection == null || selection.trim().isEmpty()) && cmbProduct.getEditor() != null) {
            selection = cmbProduct.getEditor().getText();
        }
        if (selection != null && currentProducts != null) {
            String sel = selection.trim();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (display.equalsIgnoreCase(sel) || dto.getName().equalsIgnoreCase(sel)) {
                    selectedProduct = dto;
                    cmbProduct.setValue(display);
                    if (cmbProduct.getEditor() != null) {
                        cmbProduct.getEditor().setText(display);
                        cmbProduct.getEditor().positionCaret(display.length());
                    }
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
        if (ComboBoxUtil.isNavigationKey(event)) return;
        String searchText = cmbProduct.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllProducts();
            if (productResetTimer != null) productResetTimer.stop();
            cmbProduct.show();
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
        resolveSelectedCustomer();
        if (selectedCustomer != null) {
            String display = selectedCustomer.getName() + " - " + (selectedCustomer.getContact() != null ? selectedCustomer.getContact() : "");
            cmbCustomer.setValue(display);
            if (cmbCustomer.getEditor() != null) {
                cmbCustomer.getEditor().setText(display);
                cmbCustomer.getEditor().positionCaret(display.length());
            }
            javafx.application.Platform.runLater(() -> {
                if (cmbProduct.getEditor() != null) {
                    cmbProduct.getEditor().requestFocus();
                } else {
                    cmbProduct.requestFocus();
                }
            });
        }
    }

    private void resolveSelectedCustomer() {
        if (currentCustomers == null) {
            selectedCustomer = null;
            checkCustomerDue();
            return;
        }

        String selection = cmbCustomer.getValue();
        String editorText = cmbCustomer.getEditor() != null ? cmbCustomer.getEditor().getText() : null;

        java.util.ArrayList<String> candidates = new java.util.ArrayList<>();
        if (editorText != null && !editorText.trim().isEmpty()) candidates.add(editorText.trim());
        if (selection != null && !selection.trim().isEmpty() && !candidates.contains(selection.trim())) candidates.add(selection.trim());

        if (candidates.isEmpty()) {
            selectedCustomer = null;
            checkCustomerDue();
            return;
        }

        for (String candidate : candidates) {
            CustomerDto exact = findExactCustomerMatch(candidate);
            if (exact != null) {
                selectedCustomer = exact;
                checkCustomerDue();
                return;
            }
        }

        for (String candidate : candidates) {
            CustomerDto unique = findUniquePartialCustomerMatch(candidate);
            if (unique != null) {
                selectedCustomer = unique;
                if (cmbCustomer.getEditor() != null) {
                    cmbCustomer.getEditor().setText(unique.getName() + " - " + (unique.getContact() != null ? unique.getContact() : ""));
                }
                checkCustomerDue();
                return;
            }
        }

        selectedCustomer = null;
        checkCustomerDue();
    }

    private String customerDisplay(CustomerDto dto) {
        String name = dto.getName() != null ? dto.getName() : "";
        String contact = dto.getContact() != null ? dto.getContact() : "";
        return name + " - " + contact;
    }

    private CustomerDto findExactCustomerMatch(String candidate) {
        for (CustomerDto dto : currentCustomers) {
            if (customerDisplay(dto).equalsIgnoreCase(candidate)) return dto;
        }
        for (CustomerDto dto : currentCustomers) {
            if (dto.getName() != null && dto.getName().equalsIgnoreCase(candidate)) return dto;
        }
        for (CustomerDto dto : currentCustomers) {
            if (dto.getContact() != null && dto.getContact().equalsIgnoreCase(candidate)) return dto;
        }
        return null;
    }

    private CustomerDto findUniquePartialCustomerMatch(String candidate) {
        String c = candidate.toLowerCase();
        CustomerDto found = null;
        for (CustomerDto dto : currentCustomers) {
            String display = customerDisplay(dto).toLowerCase();
            String name = dto.getName() != null ? dto.getName().toLowerCase() : "";
            String contact = dto.getContact() != null ? dto.getContact().toLowerCase() : "";
            boolean matches = display.contains(c) || name.contains(c) || contact.contains(c);
            if (matches) {
                if (found != null && !found.getCustId().equals(dto.getCustId())) {
                    return null;
                }
                found = dto;
            }
        }
        return found;
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

    private void installFocusFlowHandlers() {
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> productShiftTab = event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.TAB && event.isShiftDown()) {
                event.consume();
                if (cmbCustomer.getEditor() != null) {
                    cmbCustomer.getEditor().requestFocus();
                } else {
                    cmbCustomer.requestFocus();
                }
            }
        };
        cmbProduct.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, productShiftTab);
        if (cmbProduct.getEditor() != null) {
            cmbProduct.getEditor().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, productShiftTab);
        }

        javafx.event.EventHandler<javafx.scene.input.KeyEvent> customerToProduct = event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                // let selection handlers resolve; then move to product if a customer is selected
                javafx.application.Platform.runLater(() -> {
                    if (selectedCustomer != null) {
                        if (cmbProduct.getEditor() != null) {
                            cmbProduct.getEditor().requestFocus();
                        } else {
                            cmbProduct.requestFocus();
                        }
                    }
                });
            }
        };
        cmbCustomer.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, customerToProduct);
        if (cmbCustomer.getEditor() != null) {
            cmbCustomer.getEditor().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, customerToProduct);
        }
    }

    @FXML
    void cmbCustomerOnKeyReleased(KeyEvent event) {
        if (ComboBoxUtil.isNavigationKey(event)) return;
        String searchText = cmbCustomer.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllCustomers();
            if (customerResetTimer != null) customerResetTimer.stop();
            cmbCustomer.show();
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

            selectedProduct = null;
            cmbProduct.getItems().clear();
            cmbProduct.getSelectionModel().clearSelection();
            if (cmbProduct.getEditor() != null) cmbProduct.getEditor().clear();
            loadProducts();
            if (cmbProduct.getEditor() != null) {
                cmbProduct.getEditor().requestFocus();
            } else {
                cmbProduct.requestFocus();
            }
            if (!cmbProduct.getItems().isEmpty()) {
                cmbProduct.show();
            }
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

            CustomerDto previous = selectedCustomer;
            selectedCustomer = null;
            cmbCustomer.getItems().clear();
            cmbCustomer.getSelectionModel().clearSelection();
            if (cmbCustomer.getEditor() != null) cmbCustomer.getEditor().clear();
            loadCustomers();

            // If demo/previous auto-selection happened in loadCustomers but user likely wants to pick newly added one,
            // keep list ready for selection and focus the field.
            if (cmbCustomer.getEditor() != null) {
                cmbCustomer.getEditor().requestFocus();
            } else {
                cmbCustomer.requestFocus();
            }
            if (!cmbCustomer.getItems().isEmpty()) {
                cmbCustomer.show();
            }
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

    private void repopulateAllProducts() {
        if (cmbProduct.getItems() == null) return;
        cmbProduct.getItems().clear();
        if (currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
        }
    }

    private void repopulateAllCustomers() {
        if (cmbCustomer.getItems() == null) return;
        cmbCustomer.getItems().clear();
        if (currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
            }
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
        repopulateAllProducts();
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
        resolveSelectedCustomer();
        if (selectedCustomer == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a valid customer from the list first!");
            return;
        }

        if (obList.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }

        try {
            String totalText = lblNetTotal.getText().replace("Total: Rs. ", "").trim();
            double netTotal = Double.parseDouble(totalText);

            boolean isDemoCustomer = AppConstants.DEMO_CUSTOMER_ID.equals(selectedCustomer.getCustId());
            String fxmlPath = isDemoCustomer ? "/view/BalanceDemoCust.fxml" : "/view/balance.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (isDemoCustomer) {
                BalanceDemoCustController demoController = loader.getController();
                demoController.setOrderData(obList, selectedCustomer, netTotal);
            } else {
                BalanceController balanceController = loader.getController();
                balanceController.setOrderData(obList, selectedCustomer, netTotal);
            }

            Stage balanceStage = new Stage();
            balanceStage.setScene(new Scene(root));
            balanceStage.setTitle("Payment / Balance");
            balanceStage.setResizable(false);
            balanceStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            balanceStage.showAndWait();

            boolean orderDone = false;
            if (isDemoCustomer) {
                orderDone = ((BalanceDemoCustController) loader.getController()).isOrderPlaced();
            } else {
                orderDone = ((BalanceController) loader.getController()).isOrderPlaced();
            }

            if (orderDone) {
                Stage stage = (Stage) tblCart.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }
}
