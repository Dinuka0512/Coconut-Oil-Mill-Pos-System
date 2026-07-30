package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.view.tm.DashboardTM;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    public static UserDto userData;

    @FXML
    private AnchorPane body;

    @FXML
    private AnchorPane bodyContent;

    @FXML
    private Label lblUserEmail;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblSalesToday;

    @FXML
    private Label lblWeeklySales;

    @FXML
    private Label lblTotalCustomers;

    @FXML
    private Label lblTotalProducts;

    @FXML
    private Label lblTotalDue;

    @FXML
    private Button btnBackupData;

    @FXML
    private TableView<DashboardTM> tblRecentSales;

    @FXML
    private TableColumn<DashboardTM, String> colDate;

    @FXML
    private TableColumn<DashboardTM, String> colOrderId;

    @FXML
    private TableColumn<DashboardTM, String> colCustomer;

    @FXML
    private TableColumn<DashboardTM, Double> colQty;

    @FXML
    private TableColumn<DashboardTM, Double> colTotal;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private LineChart<String, Number> lineChart;

    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);

    private ArrayList<OrderDto> cachedOrders;
    private ArrayList<CustomerDto> cachedCustomers;
    private ArrayList<ProductDto> cachedProducts;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateUserDisplay();
        setupTable();
        loadDashboardDataFast();
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadDashboardDataFast() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                cachedOrders = orderBO.getAllOrders();
                cachedCustomers = customerBO.getAllCustomers();
                cachedProducts = productBO.getAllProducts();
                return null;
            }
        };

        loadTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                loadSalesData();
                loadCounts();
                loadRecentSales();
                loadWeeklySalesChart();
                loadMonthlySalesLineChart();
            });
        });

        loadTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error loading dashboard data");
                loadTask.getException().printStackTrace();
            });
        });

        new Thread(loadTask).start();
    }

    private void loadSalesData() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        double todaySales = 0;
        double weeklySales = 0;

        for (OrderDto order : cachedOrders) {
            if (order.getDate() != null) {
                LocalDate orderDate = order.getDate();
                if (orderDate.equals(today)) {
                    todaySales += order.getAmountPaid();
                }
                if (!orderDate.isBefore(weekStart) && !orderDate.isAfter(today)) {
                    weeklySales += order.getAmountPaid();
                }
            }
        }

        lblSalesToday.setText(String.format("Rs.%,.2f", todaySales));
        lblWeeklySales.setText(String.format("Rs.%,.2f", weeklySales));
    }

    private void loadCounts() {
        lblTotalCustomers.setText(String.valueOf(cachedCustomers.size()));
        lblTotalProducts.setText(String.valueOf(cachedProducts.size()));
        double totalDue = cachedCustomers.stream().mapToDouble(CustomerDto::getDueAmount).sum();
        lblTotalDue.setText(String.format("Rs.%,.2f", totalDue));
    }

    private void loadRecentSales() {
        ObservableList<DashboardTM> data = FXCollections.observableArrayList();

        cachedOrders.stream()
                .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                .limit(5)
                .forEach(order -> {
                    double totalQty = 0;
                    if (order.getOrderProducts() != null) {
                        for (var detail : order.getOrderProducts()) {
                            totalQty += detail.getQuantity();
                        }
                    }
                    data.add(new DashboardTM(
                            order.getDate().toString(),
                            order.getOrderId(),
                            order.getCustomerName(),
                            totalQty,
                            order.getAmountPaid()
                    ));
                });

        tblRecentSales.setItems(data);
    }

    private void loadWeeklySalesChart() {
        LocalDate today = LocalDate.now();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            final LocalDate finalDate = date;
            double daySales = cachedOrders.stream()
                    .filter(o -> o.getDate() != null && o.getDate().equals(finalDate))
                    .mapToDouble(OrderDto::getAmountPaid)
                    .sum();

            series.getData().add(new XYChart.Data<>(dayName, daySales));
        }

        barChart.getData().clear();
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
    }

    private void loadMonthlySalesLineChart() {
        LocalDate today = LocalDate.now();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");

        for (int i = 5; i >= 0; i--) {
            LocalDate date = today.minusMonths(i);
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int monthValue = date.getMonthValue();
            int yearValue = date.getYear();

            double monthSales = cachedOrders.stream()
                    .filter(o -> o.getDate() != null)
                    .filter(o -> {
                        LocalDate od = o.getDate();
                        return od.getMonthValue() == monthValue && od.getYear() == yearValue;
                    })
                    .mapToDouble(OrderDto::getAmountPaid)
                    .sum();

            series.getData().add(new XYChart.Data<>(monthName, monthSales));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
    }

    @FXML
    void btnBackupDataOnAction(ActionEvent event) {
        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Backup feature coming soon!");
    }

    public void updateUserDisplay() {
        if (userData != null) {
            lblUserName.setText(userData.getName());
            lblUserEmail.setText(userData.getEmail());
        }
    }

    @FXML
    void logOut(ActionEvent event) {
        mainNavigateTo("/view/login.fxml");
        userData = null;
    }

    private void mainNavigateTo(String path) {
        try {
            AnchorPane dashboardPane = FXMLLoader.load(getClass().getResource(path));
            body.getChildren().clear();
            body.getChildren().add(dashboardPane);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot load...");
        }
    }

    private void subNavigateTo(String path) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(path));
            bodyContent.getChildren().clear();
            bodyContent.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot load...");
        }
    }

    @FXML
    void goToSettings(ActionEvent event) {
        SettingsController.dashboardTemp(body);
        if (userData != null && "admin".equals(userData.getRole())) {
            subNavigateTo("/view/adminSettings.fxml");
        } else {
            subNavigateTo("/view/settings.fxml");
        }
    }

    @FXML
    void goToHome(ActionEvent actionEvent) {
        mainNavigateTo("/view/dashboard.fxml");
    }

    @FXML
    void goToCustomer(ActionEvent event) {
        subNavigateTo("/view/customers.fxml");
    }

    @FXML
    void goToInventory(ActionEvent event) {
        subNavigateTo("/view/inventory.fxml");
    }

    @FXML
    void goToOrder(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/pos.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Invoice");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open invoice window!");
        }
    }

    @FXML
    void goToQuotation(ActionEvent event) {
        subNavigateTo("/view/quotation.fxml");
    }

    @FXML
    void goToPrevious(ActionEvent event) {
        subNavigateTo("/view/OrderManage.fxml");
    }

    @FXML
    void goToDueManagement(ActionEvent event) {
        subNavigateTo("/view/dueManagement.fxml");
    }
}