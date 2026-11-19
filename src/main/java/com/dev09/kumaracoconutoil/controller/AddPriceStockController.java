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
 * Small modal FXML form used to add a new price (+ stock) pair to a saved product,
 * or to edit an existing pair when opened with {@link #initDataForEdit}.
 */
public class AddPriceStockController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Label lblTitle;
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
    private boolean priceAdded;

    // When opened in edit mode the form only collects the new values; the caller
    // persists them against the edited row index.
    private boolean editMode;
    private Double editedPrice;
    private Double editedStock;

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
        this.priceAdded = false;
        this.editMode = false;
        this.editedPrice = null;
        this.editedStock = null;
        lblProduct.setText(productName == null ? "-" : productName);
        lblTitle.setText("Add New Price & Stock");
        btnSave.setText("Add Price");
    }

    /** Open the same form pre-filled with an existing price/stock pair. */
    public void initDataForEdit(String productId, String productName, double price, double qty) {
        initData(productId, productName);
        this.editMode = true;
        txtPrice.setText(formatValue(price));
        txtStock.setText(formatValue(qty));
        lblTitle.setText("Edit Price & Stock");
        btnSave.setText("Update Price");
    }

    /** True when the form was closed after a price/stock pair was submitted. */
    public boolean isPriceAdded() {
        return priceAdded;
    }

    /** Edited values returned to the caller; only set in edit mode. */
    public Double getEditedPrice() {
        return editedPrice;
    }

    public Double getEditedStock() {
        return editedStock;
    }

    private String formatValue(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
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
        if (editMode) {
            // The product page owns the row index, so it writes these values to
            // the database and refreshes the table after this dialog closes.
            editedPrice = price;
            editedStock = stock;
            priceAdded = true;
            closeStage();
            return;
        }
        if (!productBO.addPriceQty(productId, price, stock)) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Could not add the price. Please try again.");
            return;
        }
        priceAdded = true;
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
