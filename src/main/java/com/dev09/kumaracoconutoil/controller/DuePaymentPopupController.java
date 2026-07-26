package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.DuePaymentBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DuePaymentPopupController implements Initializable {

    @FXML private Label lblCustomerName;
    @FXML private Label lblCurrentDue;
    @FXML private Label lblCustomerId;
    @FXML private TextField txtPayAmount;
    @FXML private TextField txtNote;
    @FXML private Button btnPay;
    @FXML private TableView<DuePaymentEntity> tblDuePayments;
    @FXML private TableColumn<DuePaymentEntity, LocalDate> colDate;
    @FXML private TableColumn<DuePaymentEntity, String> colType;
    @FXML private TableColumn<DuePaymentEntity, String> colOrderId;
    @FXML private TableColumn<DuePaymentEntity, Double> colAmount;
    @FXML private TableColumn<DuePaymentEntity, String> colNote;

    private final DuePaymentBO duePaymentBO = (DuePaymentBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.DUE_PAYMENT);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.ORDER);

    private String customerId;
    private double currentDue;
    private boolean paymentMade = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(param -> new TableCell<DuePaymentEntity, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else if ("DUE_ADDED".equals(item)) {
                    setText("Due");
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    setText("Paid");
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });
        colOrderId.setCellValueFactory(cellData -> {
            DuePaymentEntity entity = cellData.getValue();
            if (entity.getOrder() != null) {
                return new javafx.beans.binding.StringBinding() {
                    @Override
                    protected String computeValue() {
                        return entity.getOrder().getOrderId();
                    }
                };
            }
            return new javafx.beans.binding.StringBinding() {
                @Override
                protected String computeValue() {
                    return "-";
                }
            };
        });
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(param -> new TableCell<DuePaymentEntity, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    DuePaymentEntity entity = getTableView().getItems().get(getIndex());
                    if ("DUE_ADDED".equals(entity.getType())) {
                        setText("-" + String.format("%.2f", item));
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("+" + String.format("%.2f", item));
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    public void setCustomerData(String customerId, String customerName, double currentDue) {
        this.customerId = customerId;
        this.currentDue = currentDue;

        lblCustomerId.setText(customerId);
        lblCustomerName.setText(customerName);
        updateDueDisplay();
        loadDuePayments();
    }

    private void updateDueDisplay() {
        lblCurrentDue.setText(String.format("Rs. %.2f", currentDue));
        boolean hasDue = currentDue > 0;
        btnPay.setDisable(!hasDue);
        txtPayAmount.setDisable(!hasDue);
    }

    private void loadDuePayments() {
        ArrayList<DuePaymentEntity> payments = duePaymentBO.getDuePaymentsByCustomerId(customerId);
        ObservableList<DuePaymentEntity> list = FXCollections.observableArrayList(payments);
        tblDuePayments.setItems(list);
    }

    @FXML
    void btnPayOnAction(ActionEvent event) {
        String amountText = txtPayAmount.getText().trim();
        if (amountText.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter payment amount!");
            txtPayAmount.requestFocus();
            return;
        }

        double payAmount;
        try {
            payAmount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid amount!");
            return;
        }

        if (payAmount <= 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Amount must be greater than 0!");
            return;
        }

        if (payAmount > currentDue) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Amount exceeds current due of Rs. " + String.format("%.2f", currentDue));
            return;
        }

        try {
            double remaining = orderBO.payCustomerDue(customerId, payAmount, txtNote.getText());
            double paidAmount = payAmount - remaining;
            if (paidAmount > 0) {
                paymentMade = true;
                currentDue = Math.max(0, currentDue - paidAmount);
                updateDueDisplay();
                loadDuePayments();
                txtPayAmount.clear();
                txtNote.clear();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Rs. " + String.format("%.2f", paidAmount) + " payment recorded successfully!");
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Payment failed! No unpaid orders found.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Payment failed: " + e.getMessage());
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnPay.getScene().getWindow();
        stage.close();
    }

    public boolean isPaymentMade() {
        return paymentMade;
    }
}
