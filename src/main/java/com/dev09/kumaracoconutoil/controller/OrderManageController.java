package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.controller.UpdatePosController;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.scene.control.DateCell;
import javafx.scene.input.KeyEvent;
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
    private final Set<LocalDate> orderDates = new HashSet<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        
        colAction.setCellFactory(param -> new TableCell<OrderDto, String>() {
            private final Button btnEdit = new Button("Edit");
            
            {
                btnEdit.setOnAction(event -> {
                    OrderDto order = getTableView().getItems().get(getIndex());
                    btnEditOnAction(order);
                });
                btnEdit.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
                    btnEdit.setVisible(false);
                }
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEdit);
                }
            }
        });
        
        datePicker.setValue(LocalDate.now());
        datePicker.setEditable(false);
        datePicker.setShowWeekNumbers(false);
        loadOrderDatesForMonth(YearMonth.from(LocalDate.now()));
        datePicker.setDayCellFactory(createDayCellFactory());
        datePicker.setOnAction(e -> {
            LocalDate selected = datePicker.getValue();
            if (selected != null) {
                loadOrderDatesForMonth(YearMonth.from(selected));
                loadOrdersByDate(selected);
            }
        });
        
        loadOrdersByDate(datePicker.getValue());
    }

    private Callback<DatePicker, DateCell> createDayCellFactory() {
        return param -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) return;

                if (orderDates.contains(item)) {
                    setStyle("-fx-background-color: #a8e6cf;");
                }
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    getStyleClass().add("disabled-date");
                }
            }
        };
    }

    private void loadOrderDatesForMonth(YearMonth ym) {
        try {
            orderDates.clear();
            orderDates.addAll(orderBO.getOrderDatesByMonth(ym.getYear(), ym.getMonthValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading orders: " + e.getMessage());
        }
    }

    @FXML
    void btnSearchOnAction(ActionEvent event) {
        performSearch();
    }

    @FXML
    private void txtSearchOnKeyReleased(KeyEvent event) {
        performSearch();
    }
    
    private void performSearch() {
        String searchText = txtSearch.getText().trim();
        
        if (!searchText.isEmpty()) {
            try {
                tblOrders.getItems().clear();
                orderList.clear();
                ArrayList<OrderDto> orders = orderBO.getAllOrders();
                if (orders != null) {
                    for (OrderDto order : orders) {
                        boolean match = false;
                        
                        if (order.getOrderId() != null && 
                            order.getOrderId().toLowerCase().contains(searchText.toLowerCase())) {
                            match = true;
                        }
                        if (order.getCustomerName() != null && 
                            order.getCustomerName().toLowerCase().contains(searchText.toLowerCase())) {
                            match = true;
                        }
                        if (String.valueOf(order.getTotal()).contains(searchText)) {
                            match = true;
                        }
                        
                        if (match) {
                            orderList.add(order);
                            tblOrders.getItems().add(order);
                        }
                    }
                }
                if (tblOrders.getItems().isEmpty()) {
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "No invoices found!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error searching: " + e.getMessage());
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
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select an invoice from the table!");
            return;
        }
        viewBill(selected.getOrderId());
    }

    private void viewBill(String orderId) {
        try {
            OrderEntity order = orderBO.getOrderWithProducts(orderId);
            if (order == null) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Invoice not found!");
                return;
            }
            
            List<OrderProductDetailsEntity> details = order.getOrderProducts();
            if (details == null) details = new ArrayList<>();
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Print Bill");
            alert.setHeaderText("How do you want to print this bill?");
            alert.setContentText("Select the PDF format you want to generate:");

            ButtonType smallBtn = new ButtonType("Small (80mm) PDF", ButtonBar.ButtonData.YES);
            ButtonType a4Btn = new ButtonType("Bill A4", ButtonBar.ButtonData.NO);
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(smallBtn, a4Btn, cancelBtn);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != cancelBtn) {
                byte[] pdfBytes;
                if (result.get() == smallBtn) {
                    pdfBytes = com.dev09.kumaracoconutoil.util.PdfGenerator.generateOrderBill(order, details);
                } else {
                    pdfBytes = com.dev09.kumaracoconutoil.util.PdfGenerator.generateOrderBillA4Pro(order, details);
                }
                
                File tempFile = File.createTempFile("Invoice_" + orderId, ".pdf");
                tempFile.deleteOnExit();
                java.nio.file.Files.write(tempFile.toPath(), pdfBytes);
                java.awt.Desktop.getDesktop().open(tempFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error generating bill: " + e.getMessage());
        }
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can delete invoices!");
            return;
        }
        OrderDto selected = tblOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select an invoice from the table!");
            return;
        }
        
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete invoice " + selected.getOrderId() + "?", yes, no).showAndWait();
        
        if (result.orElse(no) == yes) {
            try {
                boolean success = orderBO.deleteOrder(selected.getOrderId());
                if (success) {
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Invoice deleted successfully!");
                    loadOrderDatesForMonth(YearMonth.from(datePicker.getValue()));
                    loadOrdersByDate(datePicker.getValue());
                } else {
                    AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to delete invoice!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error deleting order: " + e.getMessage());
            }
        }
    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {
        datePicker.setValue(LocalDate.now());
        loadOrderDatesForMonth(YearMonth.now());
        loadOrdersByDate(datePicker.getValue());
        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Orders refreshed for today!");
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
    
    private void btnEditOnAction(OrderDto order) {
        if (order == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select an invoice to edit!");
            return;
        }
        if (DashboardController.userData != null && "client".equals(DashboardController.userData.getRole())) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins can edit invoices!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/updatePos.fxml"));
            Parent root = loader.load();
            
            UpdatePosController controller = loader.getController();
            controller.loadOrderForEdit(order);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Invoice - " + order.getOrderId());
            stage.setResizable(false);
            stage.setOnHidden(e -> {
                loadOrderDatesForMonth(YearMonth.from(datePicker.getValue()));
                loadOrdersByDate(datePicker.getValue());
            });
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading order: " + e.getMessage());
        }
    }
}