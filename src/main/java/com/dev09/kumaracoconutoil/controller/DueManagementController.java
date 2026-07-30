package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.PdfGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DueManagementController implements Initializable {

    @FXML private Label lblTotalOutstanding;
    @FXML private Label lblCustomersWithDues;
    @FXML private Label lblTotalCustomers;
    @FXML private TextField txtSearch;
    @FXML private TableView<CustomerDto> tblCustomers;
    @FXML private TableColumn<CustomerDto, String> colId;
    @FXML private TableColumn<CustomerDto, String> colName;
    @FXML private TableColumn<CustomerDto, String> colContact;
    @FXML private TableColumn<CustomerDto, Double> colDue;
    @FXML private TableColumn<CustomerDto, Void> colAction;

    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);

    private ObservableList<CustomerDto> masterList = FXCollections.observableArrayList();
    private FilteredList<CustomerDto> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("custId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueAmount"));
        colDue.setCellFactory(param -> new TableCell<CustomerDto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("Rs. %.2f", item));
                    if (item > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
        addActionButtons();
        setupSearch();
        loadData();
    }

    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<CustomerDto, Void>() {
            private final Button btnPay = new Button("Pay Due");
            {
                btnPay.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;");
                btnPay.setOnAction(event -> {
                    CustomerDto customer = getTableView().getItems().get(getIndex());
                    openDuePaymentPopup(customer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CustomerDto customer = getTableView().getItems().get(getIndex());
                    btnPay.setDisable(customer.getDueAmount() <= 0);
                    setGraphic(btnPay);
                }
            }
        });
    }

    private void setupSearch() {
        filteredList = new FilteredList<>(masterList, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(customer -> {
                if (newValue == null || newValue.trim().isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return customer.getCustId().toLowerCase().contains(lower)
                        || customer.getName().toLowerCase().contains(lower)
                        || customer.getContact().toLowerCase().contains(lower);
            });
        });
        SortedList<CustomerDto> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tblCustomers.comparatorProperty());
        tblCustomers.setItems(sortedList);
    }

    private void loadData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ArrayList<CustomerDto> customers = customerBO.getAllCustomers();
                javafx.application.Platform.runLater(() -> {
                    masterList.setAll(customers);
                    updateSummary();
                });
                return null;
            }
        };
        task.setOnFailed(e -> {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load customers: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    private void updateSummary() {
        double totalOutstanding = masterList.stream().mapToDouble(CustomerDto::getDueAmount).sum();
        long customersWithDues = masterList.stream().filter(c -> c.getDueAmount() > 0).count();
        lblTotalOutstanding.setText(String.format("Rs. %.2f", totalOutstanding));
        lblCustomersWithDues.setText(String.valueOf(customersWithDues));
        lblTotalCustomers.setText(String.valueOf(masterList.size()));
    }

    private void openDuePaymentPopup(CustomerDto customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DuePaymentPopup.fxml"));
            AnchorPane pane = loader.load();
            DuePaymentPopupController controller = loader.getController();
            controller.setCustomerData(customer.getCustId(), customer.getName(), customer.getDueAmount());

            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setTitle("Due Payment - " + customer.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isPaymentMade()) {
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Cannot open due payment window!");
        }
    }

    @FXML
    void btnReconcileOnAction(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                customerBO.reconcileDueAmounts();
                ArrayList<CustomerDto> customers = customerBO.getAllCustomers();
                javafx.application.Platform.runLater(() -> {
                    masterList.setAll(customers);
                    updateSummary();
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Due amounts reconciled successfully!");
                });
                return null;
            }
        };
        task.setOnFailed(e -> {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Reconciliation failed: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    @FXML
    void btnExportDueReportOnAction(ActionEvent event) throws IOException {
        ArrayList<CustomerDto> customersWithDues = masterList.stream()
                .filter(c -> c.getDueAmount() > 0)
                .sorted(Comparator.comparingDouble(CustomerDto::getDueAmount).reversed())
                .collect(Collectors.toCollection(ArrayList::new));

        if (customersWithDues.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "No customers with dues to export.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Select Format");
        alert.setHeaderText("How do you need to print?");
        alert.setContentText("Choose an option:");

        ButtonType btn80mm = new ButtonType("Small (80mm)");
        ButtonType btnA4 = new ButtonType("A4 Page");
        ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btn80mm, btnA4, btnCancel);

        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() == btnCancel) return;

        ArrayList<com.dev09.kumaracoconutoil.entity.CustomerEntity> entities = new ArrayList<>();
        for (CustomerDto dto : customersWithDues) {
            entities.add(new com.dev09.kumaracoconutoil.entity.CustomerEntity(
                    dto.getCustId(), dto.getName(), dto.getContact(), false, dto.getDueAmount()
            ));
        }

        byte[] bytes;
        String fileName;
        if (result.get() == btn80mm) {
            bytes = PdfGenerator.DuePaymentReportThermal(entities);
            fileName = "DueReport_80mm";
        } else {
            bytes = PdfGenerator.DuePaymentReportA4Pro(entities);
            fileName = "DueReport_A4";
        }

        File tempFile = File.createTempFile(fileName, ".pdf");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), bytes);
        java.awt.Desktop.getDesktop().open(tempFile);
    }
}
