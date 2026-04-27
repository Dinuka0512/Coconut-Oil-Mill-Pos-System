package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

public class OrderManageController implements Initializable {

    @FXML
    private TextField txtSearch;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView<OrderDto> tblOrders;

    @FXML
    private TableColumn<OrderDto, String> colOrderId;

    @FXML
    private TableColumn<OrderDto, String> colDate;

    @FXML
    private TableColumn<OrderDto, String> colCustomer;

    @FXML
    private TableColumn<OrderDto, String> colContact;

    @FXML
    private TableColumn<OrderDto, String> colTotal;

    @FXML
    private TableColumn<OrderDto, String> colAction;

    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ArrayList<OrderDto> orderList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        datePicker.setValue(LocalDate.now());
        datePicker.setEditable(false);
        datePicker.setShowWeekNumbers(false);
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isAfter(LocalDate.now())) {
                            setDisable(true);
                            getStyleClass().add("disabled-date");
                        }
                    }
                };
            }
        });
        datePicker.setOnAction(e -> {
            LocalDate selected = datePicker.getValue();
            if (selected != null) {
                loadOrdersByDate(selected);
            }
        });
        
        loadOrdersByDate(datePicker.getValue());
    }

    private void loadOrdersByDate(LocalDate date) {
        try {
            tblOrders.getItems().clear();
            orderList.clear();
            ArrayList<OrderDto> orders = orderBO.getAllOrders();
            if (orders != null) {
                for (OrderDto order : orders) {
                    if (order.getDate() != null && order.getDate().equals(date)) {
                        orderList.add(order);
                        tblOrders.getItems().add(order);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading orders: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        String searchText = txtSearch.getText().trim();
        
        if (!searchText.isEmpty()) {
            try {
                tblOrders.getItems().clear();
                orderList.clear();
                ArrayList<OrderDto> orders = orderBO.getAllOrders();
                if (orders != null) {
                    for (OrderDto order : orders) {
                        if (order.getOrderId() != null && 
                            order.getOrderId().toLowerCase().contains(searchText.toLowerCase())) {
                            orderList.add(order);
                            tblOrders.getItems().add(order);
                        }
                    }
                }
                if (tblOrders.getItems().isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "No orders found!").show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error searching: " + e.getMessage()).show();
            }
        } else {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                loadOrdersByDate(selectedDate);
            }
        }
    }

    @FXML
    void btnViewBillOnAction(ActionEvent event) {
        OrderDto selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an order from the table!").show();
            return;
        }
        viewBill(selected.getOrderId());
    }

    private void viewBill(String orderId) {
        try {
            byte[] pdfBytes = orderBO.generateOrderBill(orderId);
            if (pdfBytes != null) {
                File tempFile = File.createTempFile("Invoice_" + orderId, ".pdf");
                java.nio.file.Files.write(tempFile.toPath(), pdfBytes);
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                new Alert(Alert.AlertType.WARNING, "Order not found!").show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error generating bill: " + e.getMessage()).show();
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        OrderDto selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an order from the table!").show();
            return;
        }
        
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete order " + selected.getOrderId() + "?", yes, no).showAndWait();
        
        if (result.orElse(no) == yes) {
            try {
                boolean success = orderBO.deleteOrder(selected.getOrderId());
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Order deleted successfully!").show();
                    loadOrdersByDate(datePicker.getValue());
                } else {
                    new Alert(Alert.AlertType.ERROR, "Failed to delete order!").show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Error deleting order: " + e.getMessage()).show();
            }
        }
    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        datePicker.setValue(LocalDate.now());
        loadOrdersByDate(datePicker.getValue());
        new Alert(Alert.AlertType.INFORMATION, "Orders refreshed for today!").show();
    }

    @FXML
    void btnBackOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/pos.fxml"));
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}