package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.controller.ProductSelectionController;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

/**
 * Small modal popup built from {@code /view/productSelection.fxml}.
 * Shows the saved products in a dropdown with their Item Code / Name / Prices / Stock details.
 */
public final class ProductSelectionDialog {

    private static final String FXML_PATH = "/view/productSelection.fxml";

    private ProductSelectionDialog() {
    }

    public static Optional<ProductDto> show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(ProductSelectionDialog.class.getResource(FXML_PATH));
            Parent root = loader.load();
            ProductSelectionController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Select Product");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root));
            stage.setOnShown(event -> {
                if (owner != null) {
                    stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2.0);
                    stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2.0);
                }
                stage.toFront();
            });

            stage.showAndWait();

            return controller.isConfirmed()
                    ? Optional.ofNullable(controller.getSelectedProduct())
                    : Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
