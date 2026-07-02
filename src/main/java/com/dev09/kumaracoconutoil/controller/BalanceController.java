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

import com.dev09.kumaracoconutoil.entity.OrderEntity;

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
    private Label lblNetTotal;

    @FXML
    private TextField txtTaxRate;

    @FXML
    private Label lblTaxAmount;

    @FXML
    private Label lblTotalWithTax;

    @FXML
    private TextField txtAmountPaid;

    @FXML
    private Label lblBalance;

    @FXML
    private ComboBox<String> cmbPaymentMethod;

    @FXML
    private Label lblBalanceNote;

    @FXML
    private CheckBox chkPayDue;

    @FXML
    private Label lblExistingDue;

    private final TaxBO taxBO = (TaxBO) BOFactory.getInstance().getBo(BOFactory.BOType.TAX);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);

    private ObservableList<CartTM> cartItems;
    private CustomerDto customer;
    private double originalNetTotal;
    private boolean orderPlaced = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbPaymentMethod.getItems().addAll("Cash", "Card", "Credit");
        cmbPaymentMethod.getSelectionModel().selectFirst();

        loadTaxRate();

        Platform.runLater(() -> txtAmountPaid.requestFocus());
    }

    private void loadTaxRate() {
        try {
            TaxDto taxDto = taxBO.searchTax("1");
            if (taxDto != null) {
                txtTaxRate.setText(String.valueOf(taxDto.getTax()));
            }
        } catch (Exception e) {
            txtTaxRate.setText("0");
        }
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
        lblNetTotal.setText(String.format("%.2f", netTotal));

        try {
            double existingDue = orderBO.getTotalDueByCustomer(cust.getCustId());
            lblExistingDue.setText("Rs. " + String.format("%.2f", existingDue));
            lblExistingDue.setVisible(true);
            if (existingDue > 0) {
                chkPayDue.setVisible(true);
                chkPayDue.setManaged(true);
            } else {
                chkPayDue.setVisible(false);
                chkPayDue.setManaged(false);
            }
        } catch (Exception e) {
            lblExistingDue.setText("Rs. 0.00");
            lblExistingDue.setVisible(true);
            chkPayDue.setVisible(false);
            chkPayDue.setManaged(false);
        }

        recalculate();
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }

    private void recalculate() {
        double netTotal = originalNetTotal;

        double taxPercent = 0.0;
        try {
            String taxText = txtTaxRate.getText();
            if (taxText != null && !taxText.isEmpty()) {
                taxPercent = Double.parseDouble(taxText);
            }
        } catch (NumberFormatException e) {
            taxPercent = 0.0;
        }

        double taxAmount = netTotal * (taxPercent / 100.0);
        double totalWithTax = netTotal + taxAmount;

        lblTaxAmount.setText(String.format("%.2f", taxAmount));
        lblTotalWithTax.setText(String.format("%.2f", totalWithTax));

        calculateBalance();
    }

    private void calculateBalance() {
        try {
            double totalWithTax = Double.parseDouble(lblTotalWithTax.getText());
            String amountText = txtAmountPaid.getText();

            if (amountText == null || amountText.isEmpty()) {
                lblBalance.setText("0.00");
                lblBalance.setStyle("-fx-text-fill: #00a118;");
                lblBalanceNote.setText("");
                return;
            }

            double amount = Double.parseDouble(amountText);
            double balance = amount - totalWithTax;

            if (balance < 0) {
                lblBalance.setText(String.format("%.2f", Math.abs(balance)) + " Due");
                lblBalance.setStyle("-fx-text-fill: red;");
                lblBalanceNote.setText("Insufficient payment! Balance of Rs. " + String.format("%.2f", Math.abs(balance)) + " remains.");
            } else {
                lblBalance.setText(String.format("%.2f", balance));
                lblBalance.setStyle("-fx-text-fill: #00a118;");
                lblBalanceNote.setText(balance > 0 ? "Change due: Rs. " + String.format("%.2f", balance) : "Settled");
            }
        } catch (NumberFormatException e) {
            lblBalance.setText("0.00");
            lblBalanceNote.setText("");
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
    void onTaxRateChanged() {
        recalculate();
    }

    @FXML
    void onAmountPaidChanged() {
        calculateBalance();
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

            double taxPercent = Double.parseDouble(txtTaxRate.getText());
            double taxAmount = originalNetTotal * (taxPercent / 100.0);
            double totalWithTax = originalNetTotal + taxAmount;

            double amountPaid = 0.0;
            String amountText = txtAmountPaid.getText();
            if (amountText != null && !amountText.isEmpty()) {
                amountPaid = Double.parseDouble(amountText);
            }

            if ("CUST001".equals(customer.getCustId()) && amountPaid < totalWithTax) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer (CUST001) must pay in full!");
                return;
            }

            double effectivePaid = amountPaid;
            if (chkPayDue.isSelected()) {
                effectivePaid = orderBO.payCustomerDue(customer.getCustId(), amountPaid);
            }

            double balance = effectivePaid - totalWithTax;
            String paymentMethod = cmbPaymentMethod.getValue();

            ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
            for (CartTM tm : cartItems) {
                ProductDto productDto = productBO.searchProduct(tm.getCode());
                details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
            }

            OrderDto orderDto = new OrderDto(orderId, date, customer, details,
                    amountPaid, 0, taxAmount, taxPercent, balance, paymentMethod);

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

    private void closeStage() {
        Stage stage = (Stage) txtDescription.getScene().getWindow();
        stage.close();
    }
}
