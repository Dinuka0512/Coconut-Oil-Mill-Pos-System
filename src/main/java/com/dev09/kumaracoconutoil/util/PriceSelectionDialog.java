package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.controller.PriceSelectionController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

/**
 * Small modal popup that lets the user pick one of a product's saved price/qty tiers.
 *
 * <p>The UI itself lives in {@code /view/priceSelection.fxml} and is driven by
 * {@link PriceSelectionController}.</p>
 */
public final class PriceSelectionDialog {

    private static final String FXML_PATH = "/view/priceSelection.fxml";

    private PriceSelectionDialog() {
    }

    /**
     * @param priceQtyRows the product's {@code {price, qty}} rows
     * @return the selected {@code {price, qty}} row, or empty when cancelled
     */
    public static Optional<double[]> show(Window owner, String productName, double[][] priceQtyRows) {
        if (priceQtyRows == null || priceQtyRows.length == 0) {
            return Optional.empty();
        }

        try {
            FXMLLoader loader = new FXMLLoader(PriceSelectionDialog.class.getResource(FXML_PATH));
            Parent root = loader.load();

            PriceSelectionController controller = loader.getController();
            controller.initData(productName, priceQtyRows);

            Stage stage = new Stage();
            stage.setTitle("Select Unit Price");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setScene(new Scene(root));
            stage.setOnShown(event -> {
                if (owner != null) {
                    stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                    stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
                }
                stage.toFront();
                controller.onShown();
            });

            stage.showAndWait();

            return controller.isConfirmed()
                    ? Optional.ofNullable(controller.getSelectedTier())
                    : Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
