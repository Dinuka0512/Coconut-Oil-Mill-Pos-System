package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modal used to pick a product. The dropdown lists the saved products and the
 * Item Code / Name / Prices / Stock labels update as soon as a product is
 * chosen.
 */
public class ProductSelectionController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ComboBox<ProductDto> cmbProduct;
    @FXML
    private Label lblItemCode;
    @FXML
    private Label lblName;
    @FXML
    private Label lblPrice;
    @FXML
    private Label lblQty;
    @FXML
    private Button btnSelect;
    @FXML
    private Button btnCancel;

    private final ProductBO productBO = (ProductBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.PRODUCT);

    private ProductDto selectedProduct;
    private boolean confirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbProduct.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProductDto product) {
                if (product == null) return "";
                return product.getName() == null ? "-" : product.getName();
            }

            @Override
            public ProductDto fromString(String text) {
                return null;
            }
        });

        loadProducts();

        cmbProduct.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateLabels(newValue));
        updateLabels(cmbProduct.getSelectionModel().getSelectedItem());

        // Enter confirms the highlighted product; Esc closes the dropdown first.
        cmbProduct.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                if (cmbProduct.isShowing()) {
                    cmbProduct.hide();
                }
                confirm();
            } else if (event.getCode() == KeyCode.ESCAPE && !cmbProduct.isShowing()) {
                event.consume();
                closeStage();
            }
        });

        btnSelect.setDefaultButton(true);

        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                rootPane.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.ESCAPE), this::closeStage);
            }
            cmbProduct.requestFocus();
        });
    }

    private void loadProducts() {
        try {
            ArrayList<ProductDto> products = productBO.getAllProducts();
            cmbProduct.setItems(FXCollections.observableArrayList(
                    products == null ? new ArrayList<>() : products));

            if (!cmbProduct.getItems().isEmpty()) {
                cmbProduct.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Could not load products!");
        }
    }

    private void updateLabels(ProductDto product) {
        lblItemCode.setText("Item Code : " + (product == null || product.getItemcode() == null ? "-" : product.getItemcode()));
        lblName.setText("Name : " + (product == null || product.getName() == null ? "-" : product.getName()));
        lblPrice.setText("Prices (LKR) : " + (product == null ? "-" : product.getPriceArray()));
        lblQty.setText("Stock : " + (product == null ? "-" : formatQty(product.getQty())));
    }

    /** Whole numbers show without decimals, otherwise two decimal places. */
    private static String formatQty(double qty) {
        return qty == Math.rint(qty)
                ? String.valueOf((long) qty)
                : String.format(Locale.US, "%.2f", qty);
    }

    public ProductDto getSelectedProduct() {
        return selectedProduct;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    void btnSelectOnAction(ActionEvent event) {
        confirm();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStage();
    }

    private void confirm() {
        ProductDto product = cmbProduct.getSelectionModel().getSelectedItem();
        if (product == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
            cmbProduct.requestFocus();
            return;
        }
        confirmed = true;
        selectedProduct = product;
        closeStage();
    }

    private void closeStage() {
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
