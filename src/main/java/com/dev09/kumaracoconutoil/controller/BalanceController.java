package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.BankAccountBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderPaymentBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.bo.custom.TaxBO;
import com.dev09.kumaracoconutoil.dto.BankAccountDto;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderPaymentDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.TaxDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import com.dev09.kumaracoconutoil.view.tm.PaymentRowTM;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TextField txtAmountPaid1;

    @FXML
    private ComboBox<String> cmbPaymentMethod1;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblBillTotal;

    @FXML
    private Label lblPrecent;

    @FXML
    private TextField txtPayDueAmount;

    @FXML
    private ComboBox<String> duePayType;

    @FXML
    private TextField txtTax;

    @FXML
    private Button btnSaveTax;

    @FXML
    private Button btnCompleteSale;

    @FXML
    private Button btnCancel;

    @FXML private ComboBox<BankAccountDto> cmbBankAccount;
    @FXML private TextField txtCheckId;
    @FXML private Button btnAddPayment;
    @FXML private TableView<PaymentRowTM> tblPayments;
    @FXML private TableColumn<PaymentRowTM, String> colPayMethod;
    @FXML private TableColumn<PaymentRowTM, String> colPayAmount;
    @FXML private TableColumn<PaymentRowTM, String> colPayDetail;
    @FXML private TableColumn<PaymentRowTM, Void> colPayAction;
    @FXML private Label lblPaymentTableTotal;

    private final TaxBO taxBO = (TaxBO) BOFactory.getInstance().getBo(BOFactory.BOType.TAX);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final BankAccountBO bankAccountBO = (BankAccountBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.BANK_ACCOUNT);
    private final OrderPaymentBO orderPaymentBO = (OrderPaymentBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.ORDER_PAYMENT);

    /** All payment rows entered for this single order. */
    private final ObservableList<PaymentRowTM> paymentRows = FXCollections.observableArrayList();

    private ObservableList<CartTM> cartItems;
    private CustomerDto customer;
    private double originalNetTotal;
    private double currentTaxRate;
    private double loadedTaxRate;
    private double currentTotalWithTax;
    private double existingDue;
    private boolean orderPlaced = false;

    /*
     * Source-of-truth values shown on screen. These are saved, never recomputed
     * from other values:
     *   lblNetTotal          -> totalToPay
     *   lblExistingDue       -> existingDue
     *   lblPaymentTableTotal -> totalPaid
     * Balance is always derived from these three.
     */
    private double totalToPay;
    private double totalPaid;
    private double duePaid;
    /** Exact value displayed in lblBalance. Shortfalls move to {@link #newDue}, so this is 0 or change only. */
    private double balance;
    /** Shortfall added to the customer's due; printed as "New Due" on the bill. */
    private double newDue;
    /** Previous due remaining + new due; printed as "Total Due" on the bill. */
    private double totalDue;

    /** Set while code changes the due dropdown, so the FXML handler does not reset it. */
    private boolean applyingDuePaymentRule = false;

    /** Set when Enter confirms the highlighted payment method, before the combo closes. */
    private boolean paymentMethodCommitted = false;

    /** The Enter that confirmed the combo must not also fire Complete Sale. */
    private boolean ignoreNextSceneEnter = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbPaymentMethod1.getItems().addAll("Cash", "Card", "Cheque");
        cmbPaymentMethod1.getSelectionModel().select("Cash");

        // "None" means this sale does not touch the customer's old due.
        duePayType.getItems().addAll("None", "Full Payment", "Custom");
        duePayType.getSelectionModel().selectFirst();
        duePayType.setDisable(false);
        txtPayDueAmount.setDisable(true);
        txtPayDueAmount.clear();

        currentTaxRate = loadTaxRate();
        loadedTaxRate = currentTaxRate;
        txtTax.setText(String.valueOf(currentTaxRate));
        btnSaveTax.setDisable(true);

        styleComboSelectionVisible(cmbPaymentMethod1);
        styleComboSelectionVisible(duePayType);
        setupPaymentSection();

        // Amount is the first field when the screen loads.
        Platform.runLater(() -> {
            txtAmountPaid1.requestFocus();
            txtAmountPaid1.selectAll();
        });

        // Enter on the (closed) payment combo opens the list so arrow keys can be
        // used. Enter while it is open lets JavaFX confirm the highlighted value;
        // the hide listener below then moves to the field for that method.
        cmbPaymentMethod1.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.ENTER) return;
            if (cmbPaymentMethod1.isShowing()) {
                paymentMethodCommitted = true;
                ignoreNextSceneEnter = true;
            } else {
                cmbPaymentMethod1.show();
                event.consume();
            }
        });
        cmbPaymentMethod1.addEventHandler(ActionEvent.ACTION, e -> paymentMethodCommitted = true);
        cmbPaymentMethod1.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (wasShowing && !isShowing && paymentMethodCommitted) {
                paymentMethodCommitted = false;
                focusAfterPaymentMethod();
            }
        });

        duePayType.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !duePayType.isShowing()) {
                duePayType.show();
                event.consume();
            }
        });
        duePayType.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (wasShowing && !isShowing && duePayType.getValue() != null) {
                String selected = duePayType.getValue();
                Platform.runLater(() -> {
                    if ("Custom".equals(selected) && !txtPayDueAmount.isDisable()) {
                        txtPayDueAmount.requestFocus();
                    } else {
                        btnCompleteSale.requestFocus();
                    }
                });
            }
        });

        txtPayDueAmount.setOnAction(e -> {
            onDueAmountAction();
            if (btnCompleteSale != null) btnCompleteSale.requestFocus();
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

    /* ---------- Split payments ---------- */

    private void setupPaymentSection() {
        colPayMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colPayAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colPayDetail.setCellValueFactory(new PropertyValueFactory<>("detail"));
        tblPayments.setItems(paymentRows);
        tblPayments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblPayments.setPlaceholder(new Label("No payments added yet"));

        colPayAction.setCellFactory(column -> new TableCell<PaymentRowTM, Void>() {
            private final Button remove = new Button("X");

            {
                remove.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6 2 6; -fx-cursor: hand;");
                remove.setTooltip(new Tooltip("Remove payment (or select the row and press Delete)"));
                remove.setOnAction(e -> {
                    PaymentRowTM row = getTableRow() == null ? null : getTableRow().getItem();
                    if (row != null) removePaymentRow(row);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || getTableRow() == null || getTableRow().getItem() == null ? null : remove);
            }
        });

        ArrayList<BankAccountDto> accounts = bankAccountBO.getAllBankAccounts();
        cmbBankAccount.getItems().clear();
        if (accounts != null) cmbBankAccount.getItems().addAll(accounts);
        cmbBankAccount.setCellFactory(list -> new ListCell<BankAccountDto>() {
            @Override
            protected void updateItem(BankAccountDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : bankLabel(item));
            }
        });
        cmbBankAccount.setButtonCell(new ListCell<BankAccountDto>() {
            @Override
            protected void updateItem(BankAccountDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select bank account" : bankLabel(item));
            }
        });

        updateMethodSpecificFields();
        cmbPaymentMethod1.valueProperty().addListener((obs, oldValue, newValue) -> updateMethodSpecificFields());

        cmbBankAccount.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !cmbBankAccount.isShowing()) {
                cmbBankAccount.show();
                event.consume();
            }
        });
        cmbBankAccount.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (wasShowing && !isShowing && cmbBankAccount.getValue() != null) {
                Platform.runLater(() -> btnAddPayment.requestFocus());
            }
        });

        tblPayments.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
                PaymentRowTM selected = tblPayments.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    removePaymentRow(selected);
                    event.consume();
                }
            }
        });

        updatePaymentsTotal();
    }

    private String bankLabel(BankAccountDto bank) {
        String branch = bank.getBranch() == null || bank.getBranch().isBlank() ? "" : " (" + bank.getBranch() + ")";
        return bank.getBankName() + " - " + bank.getAccountNumber() + branch;
    }

    /** Show only the field that belongs to the chosen payment method. */
    private void updateMethodSpecificFields() {
        String method = cmbPaymentMethod1.getValue();
        boolean card = "Card".equals(method);
        boolean cheque = "Cheque".equals(method);
        cmbBankAccount.setDisable(!card);
        txtCheckId.setDisable(!cheque);
        if (!card) cmbBankAccount.getSelectionModel().clearSelection();
        if (!cheque) txtCheckId.clear();
    }

    @FXML
    void btnAddPaymentOnAction(ActionEvent event) {
        addPaymentFromFields();
    }

    /** Cheque: after the check ID, move to the Add Payment button. */
    @FXML
    void onCheckIdAction(ActionEvent event) {
        btnAddPayment.requestFocus();
    }

    private void addPaymentFromFields() {
        double amount;
        try {
            amount = Double.parseDouble(txtAmountPaid1.getText() == null ? "" : txtAmountPaid1.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid payment amount!");
            txtAmountPaid1.requestFocus();
            txtAmountPaid1.selectAll();
            return;
        }
        // 0 is allowed: the customer may be paying only the old due and nothing
        // toward the current bill. Negative amounts are still invalid.
        if (amount < 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Payment amount cannot be negative!");
            txtAmountPaid1.requestFocus();
            txtAmountPaid1.selectAll();
            return;
        }

        String method = cmbPaymentMethod1.getValue();
        if (method == null || method.isBlank()) method = "Cash";

        String bankId = null;
        String bankName = null;
        String checkNo = null;
        String detail = "-";

        if ("Card".equals(method)) {
            BankAccountDto selectedBank = cmbBankAccount.getValue();
            if (selectedBank == null) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a bank account for the card payment!");
                cmbBankAccount.requestFocus();
                return;
            }
            bankId = selectedBank.getBankId();
            bankName = bankLabel(selectedBank);
            detail = bankName;
        } else if ("Cheque".equals(method)) {
            String check = txtCheckId.getText() == null ? "" : txtCheckId.getText().trim();
            if (check.isEmpty()) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter the check ID for the cheque payment!");
                txtCheckId.requestFocus();
                return;
            }
            checkNo = check;
            detail = "Check ID: " + check;
        }

        paymentRows.add(new PaymentRowTM(method, String.format("%.2f", amount), detail, bankId, bankName, checkNo));

        // Clear the staged input before recalculating, otherwise the just-added
        // amount would be counted twice (once in the row, once in the text field).
        txtAmountPaid1.clear();
        txtCheckId.clear();
        updatePaymentsTotal();
        calculateBalance();
        txtAmountPaid1.requestFocus();
    }

    private void removePaymentRow(PaymentRowTM row) {
        paymentRows.remove(row);
        updatePaymentsTotal();
        calculateBalance();
        txtAmountPaid1.requestFocus();
    }

    private double paymentRowsTotal() {
        double total = 0.0;
        for (PaymentRowTM row : paymentRows) {
            try {
                total += Double.parseDouble(row.getAmount());
            } catch (NumberFormatException ignored) {
            }
        }
        return total;
    }

    private void updatePaymentsTotal() {
        totalPaid = paymentRowsTotal();
        lblPaymentTableTotal.setText(String.format("LKR %.2f", totalPaid));
    }

    private void saveOrderPayments(String orderId) {
        int failed = 0;
        for (PaymentRowTM row : paymentRows) {
            OrderPaymentDto dto = new OrderPaymentDto(
                    java.util.UUID.randomUUID().toString(),
                    orderId,
                    row.getMethod(),
                    Double.parseDouble(row.getAmount()),
                    row.getBankId(),
                    row.getBankName(),
                    row.getCheckNo()
            );
            if (!orderPaymentBO.savePayment(dto)) failed++;
        }
        if (failed > 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "Sale saved, but " + failed + " payment detail(s) could not be recorded.");
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

        existingDue = cust.getDueAmount();
        lblExistingDue.setText("Rs. " + String.format("%.2f", existingDue));

        // The dropdown replaced the old "Pay Due" checkbox: every sale starts at None.
        duePayType.getSelectionModel().selectFirst();
        onDuePayTupe();

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
        totalToPay = currentTotalWithTax;
        lblNetTotal.setText("LKR " + String.format("%.2f", totalToPay));

        calculateBalance();
    }

    /** Amount the customer is paying toward the old due right now. */
    private double rawDuePayment() throws NumberFormatException {
        String type = duePayType.getValue();
        if ("Full Payment".equals(type)) return existingDue;
        if ("Custom".equals(type)) {
            String text = txtPayDueAmount.getText();
            return (text == null || text.trim().isEmpty()) ? 0.0 : Double.parseDouble(text.trim());
        }
        return 0.0; // None - the old due is not part of this sale
    }

    private double selectedDuePayment() {
        try {
            return Math.max(0.0, Math.min(existingDue, rawDuePayment()));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Example: net total 500, old due 20, customer paid 510.
     * "Full Payment" would show 10 as a NEW due. Instead, switch to "Custom"
     * and treat 10 as payment toward the OLD due, so balance becomes 0.
     */
    private void applyFullPaymentOverflowRule() {
        if (applyingDuePaymentRule) return;
        if (!"Full Payment".equals(duePayType.getValue())) return;

        double extraPaid = totalPaid - totalToPay;
        if (extraPaid <= 0.00001 || existingDue <= 0.00001) return;

        double dueToPayNow = Math.min(existingDue, extraPaid);
        applyingDuePaymentRule = true;
        try {
            duePayType.getSelectionModel().select("Custom");
            txtPayDueAmount.setDisable(false);
            txtPayDueAmount.setPromptText("0.00");
            txtPayDueAmount.setText(String.format("%.2f", dueToPayNow));
        } finally {
            applyingDuePaymentRule = false;
        }
    }

    private void calculateBalance() {
        // Always use the saved screen values; never recompute them here.
        // If "Full Payment" would create a new due after the customer has already
        // paid more than the bill total, move that extra money to the old due.
        applyFullPaymentOverflowRule();

        String type = duePayType.getValue();
        duePaid = selectedDuePayment();

        double calculated;
        if ("Full Payment".equals(type)) {
            // (lblNetTotal + lblExistingDue) - lblPaymentTableTotal
            calculated = (totalToPay + existingDue) - totalPaid;
        } else if ("Custom".equals(type)) {
            // (lblNetTotal + amount being paid now) - lblPaymentTableTotal
            calculated = (totalToPay + duePaid) - totalPaid;
        } else {
            // None / normal: lblNetTotal - lblPaymentTableTotal
            calculated = totalToPay - totalPaid;
        }

        // Positive "calculated" is a shortfall. It is NOT shown as balance:
        // it becomes New Due on the bill and is added to the customer's due.
        newDue = Math.max(0.0, calculated);
        balance = Math.max(0.0, -calculated); // only change (overpayment)
        totalDue = Math.max(0.0, existingDue - duePaid) + newDue;

        if (balance > 0.005) {
            lblBalance.setText("LKR " + String.format("%.2f", balance));
            lblBalance.setStyle("-fx-text-fill: #16a34a;");
        } else {
            lblBalance.setText("LKR 0.00");
            lblBalance.setStyle("-fx-text-fill: #16a34a;");
        }
    }

    @FXML
    void onAmountPaidAction() {
        String text = txtAmountPaid1.getText();
        if (text == null || text.trim().isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter the received amount!");
            return;
        }
        try {
            Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid number for the amount!");
            return;
        }
        calculateBalance();
        focusPaymentMethod();
    }

    @FXML
    void onPaymentMethodAction() {
        // Navigation is handled when the dropdown closes (Enter or mouse click).
        calculateBalance();
    }

    /** Amount Enter -> payment method dropdown; arrow keys then choose the method. */
    private void focusPaymentMethod() {
        cmbPaymentMethod1.requestFocus();
        if (!cmbPaymentMethod1.isShowing()) cmbPaymentMethod1.show();
    }

    /** Method confirmed: Cash -> Add, Card -> bank account -> Add, Cheque -> check ID -> Add. */
    private void focusAfterPaymentMethod() {
        String method = cmbPaymentMethod1.getValue();

        // The ComboBox restores focus to itself after the popup closes, so move
        // focus on the next pulse; otherwise Cash would never land on Add Payment.
        Platform.runLater(() -> {
            updateMethodSpecificFields();
            if ("Card".equals(method)) {
                cmbBankAccount.requestFocus();
                if (!cmbBankAccount.isDisable() && !cmbBankAccount.getItems().isEmpty()) {
                    cmbBankAccount.show();
                }
            } else if ("Cheque".equals(method)) {
                txtCheckId.requestFocus();
                txtCheckId.selectAll();
            } else {
                btnAddPayment.requestFocus();
            }
        });
    }

    private void styleComboSelectionVisible(ComboBox<String> combo) {
        if (combo == null) return;
        final String baseStyle = "-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-border-width: 1;";
        final String focusStyle = "-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #3b82f6; -fx-border-radius: 5; -fx-border-width: 2;";
        final String selectedStyle = "-fx-background-radius: 5; -fx-background-color: white; -fx-border-color: #60a5fa; -fx-border-radius: 5; -fx-border-width: 1.5;";

        combo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(combo.getPromptText());
                    setStyle("-fx-text-fill: #94a3b8;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold;");
                }
            }
        });
        combo.setCellFactory(listView -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-text-fill: #1e293b;");
            }
        });

        Runnable updateBorder = () -> {
            if (combo.isFocused()) {
                combo.setStyle(focusStyle);
            } else if (combo.getValue() != null && !String.valueOf(combo.getValue()).isBlank()) {
                combo.setStyle(selectedStyle);
            } else {
                combo.setStyle(baseStyle);
            }
        };

        combo.focusedProperty().addListener((obs, oldV, newV) -> updateBorder.run());
        combo.valueProperty().addListener((obs, oldV, newV) -> updateBorder.run());
        combo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateBorder.run());

        // refresh display if a value is already selected
        String value = combo.getValue();
        if (value == null && combo.getSelectionModel() != null) {
            value = combo.getSelectionModel().getSelectedItem();
        }
        if (value != null) {
            combo.setValue(value);
        }
        updateBorder.run();
    }

    @FXML
    void onAmountPaidChanged() {
        calculateBalance();
    }

    @FXML
    void onDuePayTupe() {
        if (applyingDuePaymentRule) return;
        String type = duePayType.getValue();
        if (type == null) return;

        if ("None".equals(type)) {
            // Old due is not being paid in this sale; ignore it completely.
            txtPayDueAmount.setDisable(true);
            txtPayDueAmount.clear();
        } else if ("Full Payment".equals(type)) {
            txtPayDueAmount.setDisable(true);
            txtPayDueAmount.setText(String.format("%.2f", existingDue));
        } else if ("Custom".equals(type)) {
            txtPayDueAmount.setDisable(false);
            txtPayDueAmount.clear();
            txtPayDueAmount.setPromptText("0.00");
        }
        calculateBalance();
    }

    @FXML
    void onDueAmountAction() {
        enforceDueLimit(true);
        calculateBalance();
    }

    @FXML
    void onDueAmountChanged() {
        enforceDueLimit(false);
        calculateBalance();
    }

    /** Keeps the custom due amount inside 0..existingDue. */
    private boolean enforceDueLimit(boolean warn) {
        if (!"Custom".equals(duePayType.getValue())) return true;

        String text = txtPayDueAmount.getText();
        if (text == null || text.trim().isEmpty()) return true;

        try {
            double value = Double.parseDouble(text.trim());
            if (value > existingDue) {
                if (warn) {
                    AlertUtil.showAlert(Alert.AlertType.WARNING,
                            "Due payment cannot exceed existing due of Rs. " + String.format("%.2f", existingDue));
                }
                txtPayDueAmount.setText(String.format("%.2f", existingDue));
            } else if (value < 0) {
                if (warn) {
                    AlertUtil.showAlert(Alert.AlertType.WARNING, "Due payment cannot be negative!");
                }
                txtPayDueAmount.setText("0.00");
            }
            return true;
        } catch (NumberFormatException e) {
            if (warn) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid number!");
            }
            txtPayDueAmount.clear();
            return false;
        }
    }

    private boolean shortcutsInstalled = false;

    private void setupKeyboardHandlers() {
        txtAmountPaid1.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !shortcutsInstalled) {
                shortcutsInstalled = true;

                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.ENTER, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        this::btnConfirmOnAction
                );
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.ESCAPE),
                        this::btnCancelOnAction
                );
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.S, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> {
                            if (btnSaveTax != null && !btnSaveTax.isDisable()) {
                                btnUpdateAction();
                            }
                        }
                );
                // The old Pay Due checkbox is gone; Ctrl+D now jumps to the dropdown.
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.D, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> duePayType.requestFocus()
                );

                // Fast access to the method-specific fields.
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.B, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> {
                            if (!cmbBankAccount.isDisable()) cmbBankAccount.requestFocus();
                        }
                );
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.K, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> {
                            if (!txtCheckId.isDisable()) txtCheckId.requestFocus();
                        }
                );
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.P, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> {
                            txtAmountPaid1.requestFocus();
                            txtAmountPaid1.selectAll();
                        }
                );
                newScene.getAccelerators().put(
                        new javafx.scene.input.KeyCodeCombination(KeyCode.M, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                        () -> cmbPaymentMethod1.requestFocus()
                );

                newScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (ignoreNextSceneEnter && event.getCode() == KeyCode.ENTER) {
                        ignoreNextSceneEnter = false;
                        event.consume();
                        return;
                    }
                    if (event.getCode() == KeyCode.ENTER && !event.isControlDown()
                            && !txtAmountPaid1.isFocused() && !cmbPaymentMethod1.isFocused()
                            && !duePayType.isFocused()
                            && !txtPayDueAmount.isFocused() && !txtTax.isFocused()
                            && !cmbBankAccount.isFocused() && !txtCheckId.isFocused()
                            && !tblPayments.isFocused() && !btnAddPayment.isFocused()
                            && !btnCompleteSale.isFocused() && !btnCancel.isFocused()) {
                        btnConfirmOnAction();
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

            // Fast path: if nothing was added yet, turn the current fields into a row.
            if (paymentRows.isEmpty()) {
                addPaymentFromFields();
                if (paymentRows.isEmpty()) return; // validation already showed a warning
            }

            String paymentMethod = paymentRows.size() == 1
                    ? paymentRows.get(0).getMethod()
                    : "Multiple";

            double duePaymentAmount;
            try {
                duePaymentAmount = rawDuePayment();
            } catch (NumberFormatException e) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid due payment amount!");
                return;
            }
            if (duePaymentAmount < 0 || duePaymentAmount > existingDue) {
                AlertUtil.showAlert(Alert.AlertType.WARNING,
                        "Due payment must be between 0 and Rs. " + String.format("%.2f", existingDue));
                return;
            }

            // Use the saved screen values only; refresh the saved balance from them.
            duePaid = duePaymentAmount;
            calculateBalance();

            double invoicePortion = Math.max(0.0, totalPaid - duePaid);
            if (AppConstants.DEMO_CUSTOMER_ID.equals(customer.getCustId()) && invoicePortion < totalToPay) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Demo customer (" + AppConstants.DEMO_CUSTOMER_ID + ") must pay in full!");
                return;
            }

            if (duePaid > 0) {
                orderBO.payCustomerDue(customer.getCustId(), duePaid, "Due payment received");
            }

            // OrderEntity convention: negative = new due, positive = change.
            // The shortfall is saved as New Due; lblBalance stays 0 in that case.
            double orderBalance = balance - newDue;

            ArrayList<OrderProductDetailsDto> details = new ArrayList<>();
            for (CartTM tm : cartItems) {
                ProductDto productDto = productBO.searchProduct(tm.getCode());
                details.add(new OrderProductDetailsDto(null, null, productDto, tm.getQty(), tm.getUnitPrice()));
            }

            OrderDto orderDto = new OrderDto(orderId, date, customer, details,
                    totalPaid, 0, taxAmount, currentTaxRate, orderBalance, paymentMethod);

            boolean isPlaced = orderBO.placeOrder(orderDto);

            if (isPlaced) {
                orderPlaced = true;
                saveOrderPayments(orderId);
                showPrintDialog(orderId, new com.dev09.kumaracoconutoil.util.PdfGenerator.BillTotals(
                        totalToPay, totalPaid, duePaid, balance, newDue, totalDue));
                closeStage();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invoice Placement Failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    private void showPrintDialog(String orderId,
                                 com.dev09.kumaracoconutoil.util.PdfGenerator.BillTotals totals) {
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
                com.dev09.kumaracoconutoil.entity.OrderEntity order = orderBO.getOrderWithProducts(orderId);
                java.util.List<com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity> details =
                        order != null && order.getOrderProducts() != null ? order.getOrderProducts() : new java.util.ArrayList<>();

                if (result.get() == smallBtn) {
                    byte[] escpos = com.dev09.kumaracoconutoil.util.PdfGenerator.generateOrderBill(order, details, totals);
                    com.dev09.kumaracoconutoil.util.ThermalPrinter.print(escpos);
                } else {
                    byte[] pdfBytes = com.dev09.kumaracoconutoil.util.PdfGenerator.generateOrderBillA4Pro(order, details, totals);
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
