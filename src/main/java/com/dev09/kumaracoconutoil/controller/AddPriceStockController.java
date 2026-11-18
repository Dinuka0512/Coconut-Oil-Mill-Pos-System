package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Small modal FXML form used to add a new price (+ stock) pair to a saved product.
 */
public class AddPriceStockController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label lblProduct;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtStock;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;

    private final ProductBO productBO = (ProductBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.PRODUCT);

    private String productId;
    private String productName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Enter is handled on each text field: Price -> Stock -> Save.
        // The Save button is intentionally NOT the default button, otherwise
        // pressing Enter in Price would submit before Stock is filled.
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.ESCAPE) return;
            event.consume();
            handleEscape();
        });
    }

    /** Esc closes directly when empty, otherwise asks before discarding the values. */
    private void handleEscape() {
        boolean hasValues = (txtPrice.getText() != null && !txtPrice.getText().isBlank())
                || (txtStock.getText() != null && !txtStock.getText().isBlank());
        if (hasValues && !AlertUtil.confirmClose()) {
            return;
        }
        closeStage();
    }

    public void initData(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
        lblProduct.setText(productName == null ? "-" : productName);
    }

    /** Enter in the Price field moves the caret to the Stock field. */
    @FXML
    void txtPriceOnAction(ActionEvent event) {
        txtStock.requestFocus();
    }

    /** Enter in the Stock field saves (adds/updates) the price and stock pair. */
    @FXML
    void txtStockOnAction(ActionEvent event) {
        btnSaveOnAction(event);
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        Double price = parse(txtPrice.getText());
        Double stock = parse(txtStock.getText());
        if (price == null || stock == null) {
            if (price == null) {
                txtPrice.requestFocus();
            } else {
                txtStock.requestFocus();
            }
            AlertUtil.showAlert(Alert.AlertType.WARNING, "You must fill the both feilds .....");
            return;
        }
        if (productId == null || productId.isBlank()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
            return;
        }
        if (!productBO.addPriceQty(productId, price, stock)) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Could not add the price. Please try again.");
            return;
        }
        closeStage();
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) txtPrice.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private Double parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
