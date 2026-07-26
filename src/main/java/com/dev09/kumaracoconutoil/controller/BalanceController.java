package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.bo.custom.TaxBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.TaxDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class BalanceController implements Initializable {

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblCustomer;

    @FXML
    private Label lblExistingDue;

    @FXML
    private TextField txtAmountPaid;

    @FXML
    private ComboBox<String> cmbPaymentMethod;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblBalance1;

    @FXML
    private Label lblBillTotal;

    @FXML
    private Label lblPrecent;

    @FXML
    private TextField txtPayDueAmount;

    @FXML
    private CheckBox chkPayDue1;

    @FXML
    private CheckBox chkPayDue;

    @FXML
    private ComboBox<String> duePayType;

    @FXML
    private Label lblCustomer1;

    @FXML
    private TextField txtTax;

    @FXML
    private Button btnSaveTax;

    private final TaxBO taxBO = (TaxBO) BOFactory.getInstance().getBo(BOFactory.BOType.TAX);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);

    private ObservableList<CartTM> cartItems;
    private CustomerDto customer;
    private double originalNetTotal;
    private double currentTaxRate;
    private double loadedTaxRate;
    private double currentTotalWithTax;
    private double existingDue;
    private boolean orderPlaced = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbPaymentMethod.getItems().addAll("Cash", "Card", "Credit");
        cmbPaymentMethod.getSelectionModel().selectFirst();

        duePayType.getItems().addAll("Full Payment", "Custom");
        duePayType.getSelectionModel().selectFirst();
        duePayType.setDisable(true);

        currentTaxRate = loadTaxRate();
        loadedTaxRate = currentTaxRate;
        txtTax.setText(String.valueOf(currentTaxRate));
        btnSaveTax.setDisable(true);

        Platform.runLater(() -> txtAmountPaid.requestFocus());
    }

    private double loadTaxRate() {
        try {
            TaxDto taxDto = taxBO.searchTax("1");
            if (taxDto != null) {
                return taxDto.getTax();
            }
        } catch (Exception e) {
            // ignore
        }
        return 0.0;
    }

    public void setOrderData(ObservableList<CartTM> items, CustomerDto cust, double netTotal) {
        setupKeyboardHandlers();
        this.cartItems = items;
        this.customer = cust;
        this.originalNetTotal = netTotal;

        StringBuilder desc = new StringBuilder();
        int index = 1;
        for (CartTM item : items) {
            desc.append(index++).append(". ")
                .append(item.getDescription())
                .append(" - ").append(item.getQty()).append(" Kg")
                .append(" x Rs. ").append(String.format("%.2f", item.getUnitPrice()))
                .append(" = Rs. ").append(String.format("%.2f", item.getTotal()))
                .append("\n");
        }
        txtDescription.setText(desc.toString());

        lblCustomer.setText(cust.getName());

        existingDue = cust.getDueAmount();
        lblExistingDue.setText("Rs. " + String.format("%.2f", existingDue));

        if (existingDue > 0) {
            chkPayDue.setDisable(false);
            chkPayDue.setSelected(false);
            txtPayDueAmount.setDisable(true);
            txtPayDueAmount.clear();
            txtPayDueAmount.setPromptText("0.00");
        } else {
            chkPayDue.setDisable(true);
            chkPayDue.setSelected(false);
            txtPayDueAmount.setDisable(true);
        }

        recalculate();
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }

    private void recalculate() {
        double taxAmount = originalNetTotal * (currentTaxRate / 100.0);
        currentTotalWithTax = originalNetTotal + taxAmount;

        lblBillTotal.setText(String.format("%.2f", originalNetTotal));
        lblPrecent.setText(String.format("%.2f", taxAmount));
        lblBalance1.setText("LKR " + String.format("%.2f", currentTotalWithTax));

        calculateBalance();
    }

    private void calculateBalance() {
        try {
            String amountText = txtAmountPaid.getText();

            double totalOwed = currentTotalWithTax + existingDue;

            if (amountText == null || amountText.isEmpty()) {
                lblBalance.setText("0.00");
                lblBalance.setStyle("-fx-text-fill: #16a34a;");
                return;
            }

            double amount = Double.parseDouble(amountText);

            double duePaymentAmount = 0.0;
            if (!txtPayDueAmount.isDisable() && txtPayDueAmount.getText() != null && !txtPayDueAmount.getText().isEmpty()) {
                duePaymentAmount = Double.parseDouble(txtPayDueAmount.getText());
            } else if ("Full Payment".equals(duePayType.getValue())) {
                duePaymentAmount = existingDue;
            }

            double balance = (amount + duePaymentAmount) - totalOwed;

            if (balance < 0) {
                lblBalance.setText(String.format("%.2f", Math.abs(balance)) + " Due");
                lblBalance.setStyle("-fx-text-fill: red;");
            } else {
                lblBalance.setText(String.format("%.2f", balance));
                lblBalance.setStyle("-fx-text-fill: #16a34a;");
            }
        } catch (NumberFormatException e) {
            lblBalance.setText("0.00");
        }
    }

    @FXML
    void onAmountPaidAction() {
        String text = txtAmountPaid.getText();
        if (text == null || text.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter the received amount!");
            return;
        }
        try {
            Double.parseDouble(text);
            cmbPaymentMethod.requestFocus();
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid number for the amount!");
        }
    }

    @FXML
    void onPaymentMethodAction() {
        calculateBalance();
    }

    @FXML
    void onAmountPaidChanged() {
        calculateBalance();
    }

    @FXML
    void isPayDuePayment() {
        boolean selected = chkPayDue.isSelected();
        duePayType.setDisable(!selected);
        if (selected) {
            duePayType.getSelectionModel().selectFirst();
            onDuePayTupe();
        } else {
            txtPayDueAmount.setDisable(true);
            txtPayDueAmount.clear();
            duePayType.getSelectionModel().clearSelection();
            calculateBalance();
        }
    }

    @FXML
    void onDuePayTupe() {
        String type = duePayType.getValue();
        if (type == null) return;

        if ("Full Payment".equals(type)) {
            txtPayDueAmount.setDisable(true);
            txtPayDueAmount.setText(String.format("%.2f", existingDue));
            lblBalance.setText("0.00");
            lblBalance.setStyle("-fx-text-fill: #16a34a;");
        } else if ("Custom".equals(type)) {
            txtPayDueAmount.setDisable(false);
            txtPayDueAmount.clear();
            txtPayDueAmount.setPromptText("0.00");
            txtPayDueAmount.requestFocus();
            calculateBalance();
        }
    }

    @FXML
    void onDueAmountAction() {
        String text = txtPayDueAmount.getText();
        if (text != null && !text.isEmpty()) {
            try {
                double duePay = Double.parseDouble(text);
                if (duePay > existingDue) {
                    AlertUtil.showAlert(Alert.AlertType.WARNING, "Due payment cannot exceed existing due of Rs. " + String.format("%.2f", existingDue));
                    txtPayDueAmount.setText(String.format("%.2f", existingDue));
                }
            } catch (NumberFormatException e) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid number!");
                txtPayDueAmount.clear();
            }
        }
    }

    private void setupKeyboardHandlers() {
        txtAmountPaid.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER && !txtAmountPaid.isFocused() && !cmbPaymentMethod.isFocused()) {
                        btnConfirmOnAction();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        btnCancelOnAction();
                    }
                });
            }
        });
    }

    @FXML
    void btnConfirmOnAction() {
        if (cartItems == null || customer == null) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Order data is missing!");
            return;
        }

        try {
            String orderId = orderBO.generateNextOrderId();
            LocalDate date = LocalDate.now();

            double taxAmount = originalNetTotal * (currentTaxRate / 100.0);

            double amountPaid = 0.0;
            String amountText = txtAmountPaid.getText();
            if (amountText != null && !amountText.isEmpty()) {
                amountPaid = Double.parseDouble(amountText);
            }

            double duePaymentAmount = 0.0;
            if (existingDue > 0 && txtPayDueAmount.isVisible() && !txtPayDueAmount.isDisable()) {
                String dueText = txtPayDueAmount.getText();
                if (dueText != null && !dueText.isEmpty()) {
                    duePaymentAmount = Double.parseDouble(dueText);
                    if (duePaymentAmount > existingDue) {
                        AlertUtil.showAlert(Alert.AlertType.WARNING, "Due payment cannot exceed existing due of Rs. " + String.format("%.2f", existingDue));
                        return;
                    }
                }
            }

            if ("CUST001".equals(customer.getCustId()) && amountPaid < currentTotalWithTax) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer (CUST001) must pay in full!");
                return;
            }

            if (duePaymentAmount > 0) {
                orderBO.payCustomerDue(customer.getCustId(), duePaymentAmount, "Due payment received");
            }

            double balance = amountPaid - currentTotalWithTax;
            String paymentMethod = cmbPaymentMethod.getValue();

            ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
            for (CartTM tm : cartItems) {
                ProductDto productDto = productBO.searchProduct(tm.getCode());
                details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
            }

            OrderDto orderDto = new OrderDto(orderId, date, customer, details,
                    amountPaid, 0, taxAmount, currentTaxRate, balance, paymentMethod);

            boolean isPlaced = orderBO.placeOrder(orderDto);

            if (isPlaced) {
                orderPlaced = true;
                showPrintDialog(orderId);
                closeStage();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invoice Placement Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void showPrintDialog(String orderId) {
        Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
        printAlert.setTitle("Print Bill");
        printAlert.setHeaderText("Invoice Placed Successfully!");
        printAlert.setContentText("Choose PDF format:");

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
    }

    @FXML
    void btnCancelOnAction() {
        closeStage();
    }

    @FXML
    void onTaxChanged() {
        String taxText = txtTax.getText();
        if (taxText == null || taxText.isEmpty()) {
            btnSaveTax.setDisable(true);
            btnSaveTax.setText("Update");
            return;
        }
        try {
            double newTax = Double.parseDouble(taxText);
            boolean changed = newTax != loadedTaxRate;
            btnSaveTax.setDisable(!changed);
            btnSaveTax.setText(changed ? "Save" : "Update");
        } catch (NumberFormatException e) {
            btnSaveTax.setDisable(true);
            btnSaveTax.setText("Update");
        }
    }

    @FXML
    void btnUpdateAction() {
        String taxText = txtTax.getText();
        if (taxText == null || taxText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter a tax rate!");
            return;
        }
        try {
            double newTax = Double.parseDouble(taxText);
            TaxDto taxDto = taxBO.searchTax("1");
            boolean updated;
            if (taxDto != null) {
                taxDto.setTax(newTax);
                updated = taxBO.updateTax(taxDto);
            } else {
                updated = taxBO.saveTax(new TaxDto(1, newTax));
            }
            if (updated) {
                currentTaxRate = newTax;
                loadedTaxRate = newTax;
                btnSaveTax.setDisable(true);
                btnSaveTax.setText("Update");
                recalculate();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Tax updated to " + newTax + "%");
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to update tax!");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid tax rate!");
        }
    }

    private void closeStage() {
        Stage stage = (Stage) txtDescription.getScene().getWindow();
        stage.close();
    }
}
