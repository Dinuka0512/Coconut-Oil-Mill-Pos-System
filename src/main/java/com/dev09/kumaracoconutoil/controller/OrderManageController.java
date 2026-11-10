package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AuditCell;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.view.tm.ProductSalesTM;
import com.dev09.kumaracoconutoil.controller.UpdatePosController;
import javafx.collections.ObservableList;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @FXML
    private TableColumn<OrderDto, String> colCreated;

    @FXML
    private TableColumn<OrderDto, String> colUpdated;

    @FXML
    private TableView<ProductSalesTM> tblTdySales;

    @FXML
    private TableColumn<ProductSalesTM, String> colProductId;

    @FXML
    private TableColumn<ProductSalesTM, String> colProductName;

    @FXML
    private TableColumn<ProductSalesTM, Double> colSoldQty;

    @FXML
    private TableColumn<ProductSalesTM, Double> colSoldTotal;

    @FXML
    private Button btnViewOption;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnViewBill;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnSummaryReport;

    @FXML
    private javafx.scene.layout.AnchorPane tdySummary;

    @FXML
    private javafx.scene.layout.AnchorPane rootPane;

    @FXML
    private Label lblSummaryTotal;

    @FXML
    private Label lblSummarySoldCount;

    private static final String ORDERS_VIEW_TEXT = "View Products Sales";

    private static final String PRODUCT_SALES_VIEW_TEXT = "View Summary";

    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final ArrayList<OrderDto> orderList = new ArrayList<>();
    private final Set<LocalDate> orderDates = new HashSet<>();
    private boolean productSalesView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdInfo"));
        colUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedInfo"));
        colCreated.setCellFactory(AuditCell.factory());
        colUpdated.setCellFactory(AuditCell.factory());
        boolean canSeeAudit = isAdmin();
        colCreated.setVisible(canSeeAudit);
        colUpdated.setVisible(canSeeAudit);
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colAction.setCellFactory(param -> new TableCell<OrderDto, String>() {
            private final Button btnEdit = new Button("Edit");
            
            {
                btnEdit.setOnAction(event -> {
                    OrderDto order = getTableView().getItems().get(getIndex());
                    btnEditOnAction(order);
                });
                btnEdit.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
                if (!canManageInvoices()) {
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
        
        colProductId.setCellValueFactory(new PropertyValueFactory<>("itemCode"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSoldQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colSoldTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colSoldQty.setCellFactory(createNumberCellFactory());
        colSoldTotal.setCellFactory(createNumberCellFactory());
        tblTdySales.setRowFactory(tv -> {
            TableRow<ProductSalesTM> row = new TableRow<>();
            row.setOnMousePressed(e -> {
                if (row.isEmpty()) {
                    tv.getSelectionModel().clearSelection();
                }
            });
            return row;
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
                loadProductSalesByDate(selected);
            }
        });
        
        loadOrdersByDate(datePicker.getValue());
        loadProductSalesByDate(datePicker.getValue());
        btnDelete.setDisable(!canDeleteInvoices());
        showOrdersView();

        if (rootPane != null) {
            rootPane.setOnMousePressed(event -> {
                if (!productSalesView || tblTdySales == null) return;
                javafx.scene.Node target = event.getPickResult() == null ? null : event.getPickResult().getIntersectedNode();
                if (!isNodeWithin(target, tblTdySales)) {
                    tblTdySales.getSelectionModel().clearSelection();
                }
            });
        }
    }

    private Callback<TableColumn<ProductSalesTM, Double>, TableCell<ProductSalesTM, Double>> createNumberCellFactory() {
        return param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            }
        };
    }

    private static String currentRole() {
        return DashboardController.userData == null ? "" : String.valueOf(DashboardController.userData.getRole());
    }

    private static boolean isManager() {
        return "manager".equals(currentRole());
    }

    private static boolean isAdmin() {
        return "admin".equals(currentRole());
    }

    private static boolean canManageInvoices() {
        return isAdmin() || isManager();
    }

    private static boolean canDeleteInvoices() {
        return isAdmin();
    }

    private void showOrdersView() {
        productSalesView = false;
        tblOrders.setVisible(true);
        tblTdySales.setVisible(false);
        if (tdySummary != null) tdySummary.setVisible(false);
        if (btnSummaryReport != null) btnSummaryReport.setVisible(false);
        btnViewOption.setText(ORDERS_VIEW_TEXT);
        txtSearch.setDisable(false);
        btnSearch.setDisable(false);
        btnViewBill.setDisable(false);
        btnDelete.setDisable(!canDeleteInvoices());
    }

    private void showProductSalesView() {
        productSalesView = true;
        tblOrders.setVisible(false);
        tblTdySales.setVisible(true);
        if (tdySummary != null) tdySummary.setVisible(true);
        if (btnSummaryReport != null) btnSummaryReport.setVisible(true);
        updateSummaryLabels();
        btnViewOption.setText(PRODUCT_SALES_VIEW_TEXT);
        tblOrders.getSelectionModel().clearSelection();
        txtSearch.clear();
        txtSearch.setDisable(true);
        btnSearch.setDisable(true);
        btnViewBill.setDisable(true);
        btnDelete.setDisable(true);
    }

    @FXML
    void btnViewOptionOnAction(ActionEvent event) {
        if (productSalesView) {
            showOrdersView();
        } else {
            showProductSalesView();
        }
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

    private void loadProductSalesByDate(LocalDate date) {
        tblTdySales.getSelectionModel().clearSelection();
        ObservableList<ProductSalesTM> items = tblTdySales.getItems();
        items.clear();
        if (date == null) {
            updateSummaryLabels();
            return;
        }

        try {
            List<OrderProductDetailsEntity> details = orderBO.getProductDetailsByDate(date);
            if (details == null || details.isEmpty()) {
                updateSummaryLabels();
                return;
            }

            Map<String, double[]> totalsByProduct = new LinkedHashMap<>();
            Map<String, String> codesByProduct = new LinkedHashMap<>();
            Map<String, String> namesByProduct = new LinkedHashMap<>();

            for (OrderProductDetailsEntity detail : details) {
                ProductEntity product = detail.getProduct();
                if (product == null) continue;

                String key = product.getId();
                double[] amounts = totalsByProduct.computeIfAbsent(key, k -> new double[2]);
                amounts[0] += detail.getQuantity();
                amounts[1] += detail.getQuantity() * detail.getUnitPrice();
                codesByProduct.putIfAbsent(key, product.getItemcode());
                namesByProduct.putIfAbsent(key, product.getName());
            }

            List<ProductSalesTM> sales = new ArrayList<>();
            for (Map.Entry<String, double[]> entry : totalsByProduct.entrySet()) {
                String key = entry.getKey();
                double[] amounts = entry.getValue();
                sales.add(new ProductSalesTM(
                        codesByProduct.get(key),
                        namesByProduct.get(key),
                        amounts[0],
                        amounts[1]));
            }

            sales.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));
            items.addAll(sales);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading product sales: " + e.getMessage());
        } finally {
            updateSummaryLabels();
        }
    }

    private boolean isNodeWithin(javafx.scene.Node node, javafx.scene.Node container) {
        javafx.scene.Node current = node;
        while (current != null) {
            if (current == container) return true;
            current = current.getParent();
        }
        return false;
    }

    private void updateSummaryLabels() {
        if (lblSummaryTotal == null || lblSummarySoldCount == null) return;
        double total = 0.0;
        double soldQty = 0.0;
        if (tblTdySales != null && tblTdySales.getItems() != null) {
            for (ProductSalesTM item : tblTdySales.getItems()) {
                if (item == null) continue;
                total += item.getTotal();
                soldQty += item.getQty();
            }
        }
        lblSummaryTotal.setText(String.format("Total : Rs.%,.2f", total));
        if (Math.abs(soldQty - Math.rint(soldQty)) < 0.000001) {
            lblSummarySoldCount.setText(String.format("Sold product count : %d", (long) Math.rint(soldQty)));
        } else {
            lblSummarySoldCount.setText(String.format("Sold product count : %.2f", soldQty));
        }
    }

    @FXML
    void btnGenerateSelectedDateSummary(ActionEvent event) {
        if (!productSalesView) return;
        LocalDate date = datePicker.getValue();
        if (date == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a date!");
            return;
        }
        List<ProductSalesTM> sales = new ArrayList<>(tblTdySales.getItems());
        if (sales.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "No sales data to generate summary for the selected date!");
            return;
        }
        double total = 0.0;
        double soldQty = 0.0;
        for (ProductSalesTM item : sales) {
            total += item.getTotal();
            soldQty += item.getQty();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Generate Summary Report");
        alert.setHeaderText("Generate summary report for " + date);
        alert.setContentText("Select PDF format(s):");

        ButtonType a4Btn = new ButtonType("A4 PDF");
        ButtonType smallBtn = new ButtonType("80mm PDF");
        ButtonType bothBtn = new ButtonType("Both");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(a4Btn, smallBtn, bothBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == cancelBtn) return;

        try {
            String dateStr = date.toString();
            if (result.get() == a4Btn || result.get() == bothBtn) {
                byte[] pdf = com.dev09.kumaracoconutoil.util.PdfGenerator.SalesSummaryReportA4Pro(date, sales, total, soldQty);
                File f = File.createTempFile("SalesSummary_A4_" + dateStr + "_", ".pdf");
                f.deleteOnExit();
                java.nio.file.Files.write(f.toPath(), pdf);
                java.awt.Desktop.getDesktop().open(f);
            }
            if (result.get() == smallBtn || result.get() == bothBtn) {
                byte[] pdf = com.dev09.kumaracoconutoil.util.PdfGenerator.SalesSummaryReportThermal(date, sales, total, soldQty);
                File f = File.createTempFile("SalesSummary_80mm_" + dateStr + "_", ".pdf");
                f.deleteOnExit();
                java.nio.file.Files.write(f.toPath(), pdf);
                java.awt.Desktop.getDesktop().open(f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error generating summary report: " + e.getMessage());
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
        if (productSalesView) return;

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
        if (productSalesView) return;

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
        if (productSalesView) return;

        if (!canDeleteInvoices()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admin can delete invoices!");
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
                    loadProductSalesByDate(datePicker.getValue());
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
        loadProductSalesByDate(datePicker.getValue());
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
        if (!canManageInvoices()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Only admins and managers can edit invoices!");
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
                loadProductSalesByDate(datePicker.getValue());
            });
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading order: " + e.getMessage());
        }
    }
}