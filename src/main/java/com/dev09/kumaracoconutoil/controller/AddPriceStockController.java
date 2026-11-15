package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.ProductPriceBO;
import com.dev09.kumaracoconutoil.dto.ProductPriceDto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Small modal FXML form used to add a new price (+ stock) for a saved product.
 */
public class AddPriceStockController implements Initializable {

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

    private final ProductPriceBO productPriceBO = (ProductPriceBO) BOFactory.getInstance()
            .getBo(BOFactory.BOType.PRODUCT_PRICE);

    private String productId;
    private String productName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnSave.setDefaultButton(true);
    }

    public void initData(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
        lblProduct.setText(productName == null ? "-" : productName);
    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        Double price = parse(txtPrice.getText());
        Double stock = parse(txtStock.getText());
        if (price == null || stock == null) {
            return;
        }

        ArrayList<ProductPriceDto> rows = new ArrayList<>();
        rows.add(new ProductPriceDto(null, productId, productName, price, stock));
        boolean saved = productPriceBO.syncPrices(productId, rows);

        if (saved) {
            Stage stage = (Stage) txtPrice.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
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
