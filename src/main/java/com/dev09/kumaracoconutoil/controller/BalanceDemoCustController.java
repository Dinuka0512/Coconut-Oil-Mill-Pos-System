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

public class BalanceDemoCustController implements Initializable {

    @FXML
    private TextArea txtDescription;

    @FXML
    private Label lblCustomer;

    @FXML
    private Label lblExistingDue;

    @FXML
    private Label lblBalance1;

    @FXML
    private Label lblBillTotal;

    @FXML
    private Label lblPrecent;

    @FXML
    private TextField txtPaidAmount;

    @FXML
    private Label lblPriceBalance;

    @FXML
    private Button btnCompleteSale;

    @FXML
    private Button btnCancel;

    @FXML
    private CheckBox chkPayDue1;

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
        currentTaxRate = loadTaxRate();
        loadedTaxRate = currentTaxRate;
        txtTax.setText(String.valueOf(currentTaxRate));
        btnSaveTax.setDisable(true);

        Platform.runLater(() -> {
            txtPaidAmount.requestFocus();
            txtPaidAmount.selectAll();

            if (txtDescription.getScene() != null) {
                txtDescription.getScene().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        javafx.scene.Node focusOwner = txtDescription.getScene().getFocusOwner();
                        // These controls handle ENTER themselves (validate / complete / cancel)
                        if (focusOwner == txtPaidAmount || focusOwner == btnCompleteSale || focusOwner == btnCancel) {
                            return;
                        }
                        btnConfirmOnAction();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        btnCancelOnAction();
                    }
                });
            }
        });
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
        this.cartItems = items;
        this.customer = cust;
        this.originalNetTotal = netTotal;

        currentTaxRate = loadTaxRate();
        loadedTaxRate = currentTaxRate;
        txtTax.setText(String.valueOf(currentTaxRate));

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
        String amountText = txtPaidAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            lblPriceBalance.setText("LKR 0.00");
            lblPriceBalance.setStyle("-fx-text-fill: #16a34a;");
            return;
        }

        try {
            double balance = Double.parseDouble(amountText.trim()) - currentTotalWithTax;
            if (balance < 0) {
                lblPriceBalance.setText("LKR " + String.format("%.2f", Math.abs(balance)) + " Due");
                lblPriceBalance.setStyle("-fx-text-fill: #c90404;");
            } else {
                lblPriceBalance.setText("LKR " + String.format("%.2f", balance));
                lblPriceBalance.setStyle("-fx-text-fill: #16a34a;");
            }
        } catch (NumberFormatException e) {
            lblPriceBalance.setText("LKR 0.00");
            lblPriceBalance.setStyle("-fx-text-fill: #16a34a;");
        }
    }

    @FXML
    void onPaidAmountChanged() {
        calculateBalance();
    }

    @FXML
    void onPaidAmountAction() {
        if (getValidatedPaidAmount() >= 0 && btnCompleteSale != null) {
            btnCompleteSale.requestFocus();
        }
    }

    /** Returns the paid amount, or -1 when the input is missing, invalid or below the net total. */
    private double getValidatedPaidAmount() {
        String amountText = txtPaidAmount.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter the paid amount!");
            txtPaidAmount.requestFocus();
            return -1;
        }

        double amountPaid;
        try {
            amountPaid = Double.parseDouble(amountText.trim());
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid number for the paid amount!");
            txtPaidAmount.requestFocus();
            txtPaidAmount.selectAll();
            return -1;
        }

        if (amountPaid < currentTotalWithTax) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer canot place the due payments");
            txtPaidAmount.requestFocus();
            txtPaidAmount.selectAll();
            return -1;
        }
        return amountPaid;
    }

    @FXML
    void btnConfirmOnAction() {
        if (cartItems == null || customer == null) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Order data is missing!");
            return;
        }

        double amountPaid = getValidatedPaidAmount();
        if (amountPaid < 0) {
            return;
        }

        try {
            String orderId = orderBO.generateNextOrderId();
            LocalDate date = LocalDate.now();

            double taxAmount = originalNetTotal * (currentTaxRate / 100.0);
            double balance = amountPaid - currentTotalWithTax;

            ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
            for (CartTM tm : cartItems) {
                ProductDto productDto = productBO.searchProduct(tm.getCode());
                details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
            }

            OrderDto orderDto = new OrderDto(orderId, date, customer, details,
                    amountPaid, 0, taxAmount, currentTaxRate, balance, "Cash");

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
        printAlert.setContentText("Choose output format:");

        ButtonType smallBtn = new ButtonType("Small (80mm) - Thermal Printer", ButtonBar.ButtonData.YES);
        ButtonType a4Btn = new ButtonType("A4 PDF", ButtonBar.ButtonData.NO);
        printAlert.getButtonTypes().setAll(smallBtn, a4Btn);

        Optional<ButtonType> result = printAlert.showAndWait();
        if (result.isPresent()) {
            try {
                if (result.get() == smallBtn) {
                    byte[] escpos = orderBO.generateOrderBill80mm(orderId);
                    if (escpos != null) {
                        com.dev09.kumaracoconutoil.util.ThermalPrinter.print(escpos);
                    }
                } else {
                    byte[] pdfBytes = orderBO.generateOrderBillA4(orderId);
                    if (pdfBytes != null) {
                        File tempFile = File.createTempFile("Invoice_" + orderId, ".pdf");
                        tempFile.deleteOnExit();
                        Files.write(tempFile.toPath(), pdfBytes);
                        Desktop.getDesktop().open(tempFile);
                    }
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
