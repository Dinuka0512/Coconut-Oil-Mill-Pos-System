package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AutoLockSettings;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import com.dev09.kumaracoconutoil.util.DatabaseBackupUtil;
import com.dev09.kumaracoconutoil.util.FormState;
import com.dev09.kumaracoconutoil.util.IdleMonitor;
import com.dev09.kumaracoconutoil.util.RoleUtil;
import com.dev09.kumaracoconutoil.util.SessionRestore;

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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
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

    /** Screen currently shown in the content area, remembered for idle-logout resume. */
    private String currentSubView;
    private String homeFxml = "/view/dashboard.fxml";

    /** Invalidates shortcuts on dashboards that are no longer the active screen. */
    private static long shortcutGeneration = 0;
    private long activeShortcutGeneration = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateUserDisplay();
        int autoLockMinutes = AutoLockSettings.getMinutes();
        if (autoLockMinutes > 0) {
            IdleMonitor.start(Duration.minutes(autoLockMinutes), this::performIdleLogout);
        }
        installDashboardShortcuts();
        if (userData != null && RoleUtil.isCashier(userData.getRole())) {
            subNavigateTo("/view/inventory.fxml");
            return;
        }
        setupTable();
        loadDashboardDataFast();
    }

    /**
     * Shortcuts live on the dashboard root, so they disappear with it on logout.
     * The login screen is a different subtree and never sees these keys.
     */
    private void installDashboardShortcuts() {
        activeShortcutGeneration = ++shortcutGeneration;
        body.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (activeShortcutGeneration != shortcutGeneration || userData == null
                    || !event.isControlDown()
                    || event.isAltDown() || event.isMetaDown() || event.isShiftDown()) {
                return;
            }
            switch (event.getCode()) {
                case L -> { event.consume(); logOut(null); }
                case DIGIT1, NUMPAD1 -> { event.consume(); mainNavigateTo(homeFxml); }
                case DIGIT2, NUMPAD2 -> { event.consume(); goToCustomer(null); }
                case DIGIT3, NUMPAD3 -> { event.consume(); goToInventory(null); }
                case DIGIT4, NUMPAD4 -> { event.consume(); goToSuppliers(null); }
                case DIGIT5, NUMPAD5 -> { event.consume(); goToOrder(null); }
                case DIGIT6, NUMPAD6 -> { event.consume(); goToQuotation(null); }
                case DIGIT7, NUMPAD7 -> { event.consume(); goToDueManagement(null); }
                case DIGIT8, NUMPAD8 -> { event.consume(); goToPrevious(null); }
                case DIGIT9, NUMPAD9 -> { event.consume(); goToSettings(null); }
                default -> { }
            }
        });
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
                    todaySales += order.getTotal();
                }
                if (!orderDate.isBefore(weekStart) && !orderDate.isAfter(today)) {
                    weeklySales += order.getTotal();
                }
            }
        }

        lblSalesToday.setText(String.format("Rs.%,.2f", todaySales));
        lblWeeklySales.setText(String.format("Rs.%,.2f", weeklySales));
    }

    private void loadCounts() {
        lblTotalCustomers.setText(String.valueOf(cachedCustomers.size()));
        lblTotalProducts.setText(String.valueOf(cachedProducts.size()));
        if (lblTotalDue != null) {
            double totalDue = cachedCustomers.stream().mapToDouble(CustomerDto::getDueAmount).sum();
            lblTotalDue.setText(String.format("Rs.%,.2f", totalDue));
        }
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
                            order.getTotal()
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
                    .mapToDouble(OrderDto::getTotal)
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
                    .mapToDouble(OrderDto::getTotal)
                    .sum();

            series.getData().add(new XYChart.Data<>(monthName, monthSales));
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
    }

    @FXML
    void btnBackupDataOnAction(ActionEvent event) {
        Task<DatabaseBackupUtil.BackupResult> task = new Task<DatabaseBackupUtil.BackupResult>() {
            @Override
            protected DatabaseBackupUtil.BackupResult call() throws Exception {
                return DatabaseBackupUtil.backupDatabase();
            }
        };
        task.setOnSucceeded(e -> {
            DatabaseBackupUtil.BackupResult backup = task.getValue();
            File backupFile = backup.getFile();
            AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                    "Full database backup created successfully!\n\n"
                    + backup.getTableCount() + " tables, " + backup.getRowCount() + " rows exported.\n"
                    + "File (read-only SQL):\n" + backupFile.getAbsolutePath());
            try {
                java.awt.Desktop.getDesktop().open(backupFile.getParentFile());
            } catch (Exception ignored) {
                // folder could not be opened, ignore
            }
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Backup failed: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    public void updateUserDisplay() {
        if (userData != null) {
            lblUserName.setText(userData.getName());
            lblUserEmail.setText(userData.getEmail());
        }
    }

    @FXML
    void logOut(ActionEvent event) {
        com.dev09.kumaracoconutoil.util.UserLogUtil.record("LOGOUT");
        IdleMonitor.stop();
        SessionRestore.clear();
        mainNavigateTo("/view/login.fxml");
        userData = null;
        AuditUtil.clear();
    }

    private void performIdleLogout() {
        IdleMonitor.stop();
        Platform.runLater(() -> {
            // The dashboard can already be gone (for example after a profile update
            // navigated to login). Never capture or close windows from a stale view.
            if (body == null || body.getScene() == null) return;

            // Capture BEFORE the invoice/dialog windows are closed and before the
            // current user is cleared - the snapshot is owned by this account.
            Node activeView = bodyContent != null && !bodyContent.getChildren().isEmpty()
                    ? bodyContent.getChildren().get(0) : null;
            SessionRestore.save(SessionRestore.capture(
                    userData, homeFxml, currentSubView,
                    activeView instanceof Parent parent ? parent : null));

            com.dev09.kumaracoconutoil.util.UserLogUtil.record("AUTO_LOGOUT");
            try {
                Window mainWindow = body != null && body.getScene() != null ? body.getScene().getWindow() : null;
                for (Window w : new ArrayList<>(Window.getWindows())) {
                    if (w != mainWindow && w.isShowing()) {
                        if (w instanceof Stage) {
                            ((Stage) w).close();
                        } else {
                            w.hide();
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            userData = null;
            AuditUtil.clear();
            mainNavigateTo("/view/login.fxml");
            int minutes = AutoLockSettings.getMinutes();
            String idleText = minutes > 0
                    ? minutes + (minutes == 1 ? " minute" : " minutes")
                    : "a period";
            AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                    "You were logged out due to " + idleText + " of inactivity.\n"
                            + "Your current work will be restored when you sign in again as the same user.");
        });
    }

    /** Called by the login screen right after this dashboard is loaded. */
    public void setHomeFxml(String homeFxml) {
        this.homeFxml = homeFxml;
    }

    /** Put the user back on the same screen, with the same typed values. */
    public void applyRestore(SessionRestore.Snapshot snapshot) {
        if (snapshot == null) return;
        if (snapshot.subViewFxml() != null) {
            subNavigateTo(snapshot.subViewFxml());
            if (!bodyContent.getChildren().isEmpty()) {
                FormState.restore(bodyContent.getChildren().get(0), snapshot.subViewFields());
            }
        }
        SessionRestore.reopen(snapshot.windows());
    }

    /** Called when the dashboard is replaced (logout, profile update) so old shortcuts go quiet. */
    public static void deactivateShortcuts() {
        shortcutGeneration++;
    }

    private void mainNavigateTo(String path) {
        deactivateShortcuts();
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
            currentSubView = path;
            bodyContent.getChildren().clear();
            bodyContent.getChildren().add(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot load...");
        }
    }

    @FXML
    void goToSettings(ActionEvent event) {
        SettingsController.dashboardTemp(body);
        String role = userData == null ? "" : String.valueOf(userData.getRole());
        if ("admin".equals(role) || "manager".equals(role)) {
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
    void goToSuppliers(ActionEvent event) {
        subNavigateTo("/view/suppliers.fxml");
    }

    @FXML
    void goToInventory(ActionEvent event) {
        subNavigateTo("/view/inventory.fxml");
    }

    @FXML
    void goToOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pos.fxml"));
            Parent root = loader.load();
            SimplePOSController posController = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Invoice");
            stage.setResizable(false);
            SessionRestore.track(stage, "/view/pos.fxml", posController);
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
    void goToHistory(ActionEvent event) {
        subNavigateTo("/view/OrderManage.fxml");
    }

    @FXML
    void goToDueManagement(ActionEvent event) {
        subNavigateTo("/view/dueManagement.fxml");
    }
}