package com.dev09.kumaracoconutoil.util;

import com.dev09.kumaracoconutoil.controller.SupplierSelectionController;
import com.dev09.kumaracoconutoil.dto.SupplierDto;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

/**
 * Small modal popup built from {@code /view/supplierSelection.fxml}.
 * Shows the saved suppliers in a dropdown with their Name / Contact details.
 */
public final class SupplierSelectionDialog {

    private static final String FXML_PATH = "/view/supplierSelection.fxml";

    private SupplierSelectionDialog() {
    }

    public static Optional<SupplierDto> show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(SupplierSelectionDialog.class.getResource(FXML_PATH));
            Parent root = loader.load();
            SupplierSelectionController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Select Supplier");
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
                    ? Optional.ofNullable(controller.getSelectedSupplier())
                    : Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
