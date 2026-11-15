package com.dev09.kumaracoconutoil.controller;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for the modal "Select Unit Price" popup defined in priceSelection.fxml.
 *
 * <p>Keyboard behaviour mirrors the POS product dropdown:</p>
 * <ul>
 *     <li>Up / Down / Page Up / Page Down / Home / End move a custom highlight.</li>
 *     <li>Enter confirms the highlighted price immediately.</li>
 *     <li>Esc closes the dropdown, then the popup.</li>
 * </ul>
 *
 * <p>A single mouse click on a price confirms it straight away.</p>
 */
public class PriceSelectionController implements Initializable {

    @FXML
    private Label lblProduct;
    @FXML
    private ComboBox<Double> cmbPrice;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSelect;

    private static final String HIGHLIGHT_KEY = "price.highlightIndex";
    private static final String NAV_KEY = "price.navInstalled";

    private Double selectedPrice;
    private boolean confirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbPrice.setEditable(false);
        cmbPrice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : "LKR " + formatPrice(value);
            }

            @Override
            public Double fromString(String text) {
                return null;
            }
        });
        cmbPrice.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "LKR " + formatPrice(item));
                Object highlight = cmbPrice.getProperties().get(HIGHLIGHT_KEY);
                boolean highlighted = !empty && item != null
                        && highlight instanceof Integer index && index == getIndex();
                setStyle(highlighted
                        ? "-fx-background-color: #2563eb; -fx-text-fill: white;"
                        : "-fx-background-color: transparent; -fx-text-fill: #0f172a;");
            }
        });

        btnSelect.setDefaultButton(true);

        installKeyboardNavigation();
    }

    /**
     * Populates the popup. Must be called after the FXML has been loaded.
     */
    public void initData(String productName, List<Double> prices) {
        lblProduct.setText(productName == null ? "" : productName);
        cmbPrice.setItems(FXCollections.observableArrayList(prices));
        if (!prices.isEmpty()) {
            cmbPrice.getSelectionModel().selectFirst();
        }
    }

    /**
     * Invoked by the dialog once the stage is visible so the dropdown can open.
     *
     * <p>The very first {@code show()} call can be swallowed while the stage is still
     * settling (the popup opens and is immediately hidden again), so it is retried
     * briefly until the popup actually stays visible.</p>
     */
    public void onShown() {
        // Focus the combo (first price already selected) but do not force-open the dropdown.
        Platform.runLater(() -> cmbPrice.requestFocus());
    }

    public Double getSelectedPrice() {
        return selectedPrice;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    void btnSelectOnAction(ActionEvent event) {
        confirm(getHighlightedPrice());
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStage();
    }

    private void confirm(Double value) {
        if (confirmed || value == null) {
            return;
        }
        confirmed = true;
        selectedPrice = value;
        if (cmbPrice.isShowing()) {
            cmbPrice.hide();
        }
        closeStage();
    }

    private void closeStage() {
        if (cmbPrice.getScene() != null && cmbPrice.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void installKeyboardNavigation() {
        cmbPrice.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN || code == KeyCode.UP) {
                if (!cmbPrice.isShowing()) {
                    event.consume();
                    if (cmbPrice.getItems().isEmpty()) {
                        return;
                    }
                    int index = cmbPrice.getSelectionModel().getSelectedIndex();
                    cmbPrice.getProperties().put(HIGHLIGHT_KEY, Math.max(0, index));
                    cmbPrice.show();
                } else {
                    ListView<?> listView = getPopupListView();
                    if (listView != null) {
                        event.consume();
                        moveHighlight(listView, code == KeyCode.DOWN ? 1 : -1);
                    }
                }
            } else if (code == KeyCode.ENTER) {
                event.consume();
                confirm(getHighlightedPrice());
            } else if (code == KeyCode.ESCAPE) {
                event.consume();
                if (cmbPrice.isShowing()) {
                    cmbPrice.hide();
                } else {
                    closeStage();
                }
            }
        });

        cmbPrice.showingProperty().addListener((observable, wasShowing, isShowing) -> {
            if (isShowing) {
                installPopupNavigation();
            } else {
                cmbPrice.getProperties().remove(HIGHLIGHT_KEY);
            }
        });
    }

    private void installPopupNavigation() {
        ListView<?> listView = getPopupListView();
        if (listView == null) {
            return;
        }

        if (!Boolean.TRUE.equals(listView.getProperties().get(NAV_KEY))) {
            listView.getProperties().put(NAV_KEY, true);

            listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                KeyCode code = event.getCode();
                if (code == KeyCode.DOWN) {
                    event.consume();
                    moveHighlight(listView, 1);
                } else if (code == KeyCode.UP) {
                    event.consume();
                    moveHighlight(listView, -1);
                } else if (code == KeyCode.PAGE_DOWN) {
                    event.consume();
                    moveHighlight(listView, 5);
                } else if (code == KeyCode.PAGE_UP) {
                    event.consume();
                    moveHighlight(listView, -5);
                } else if (code == KeyCode.HOME) {
                    event.consume();
                    setHighlight(listView, 0);
                } else if (code == KeyCode.END) {
                    event.consume();
                    setHighlight(listView, cmbPrice.getItems().size() - 1);
                } else if (code == KeyCode.ENTER) {
                    event.consume();
                    confirm(getHighlightedPrice());
                } else if (code == KeyCode.ESCAPE) {
                    event.consume();
                    cmbPrice.hide();
                }
            });

            // A single mouse click confirms immediately instead of requiring a second click.
            listView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ListCell<?> cell = findCell(event);
                if (cell == null || cell.isEmpty() || !(cell.getItem() instanceof Double value)) {
                    return;
                }
                event.consume();
                confirm(value);
            });
        }

        int index = cmbPrice.getSelectionModel().getSelectedIndex();
        setHighlight(listView, index < 0 ? 0 : index);
    }

    private ListCell<?> findCell(MouseEvent event) {
        if (event.getPickResult() == null) {
            return null;
        }
        Node node = event.getPickResult().getIntersectedNode();
        while (node != null && !(node instanceof ListCell<?>)) {
            node = node.getParent();
        }
        return node instanceof ListCell<?> cell ? cell : null;
    }

    private void moveHighlight(ListView<?> listView, int delta) {
        Object highlight = cmbPrice.getProperties().get(HIGHLIGHT_KEY);
        int current = highlight instanceof Integer index ? index : 0;
        setHighlight(listView, current + delta);
    }

    private void setHighlight(ListView<?> listView, int index) {
        int size = cmbPrice.getItems().size();
        if (size == 0) {
            return;
        }
        int safeIndex = Math.max(0, Math.min(size - 1, index));
        cmbPrice.getProperties().put(HIGHLIGHT_KEY, safeIndex);
        listView.scrollTo(safeIndex);
        listView.refresh();
    }

    private Double getHighlightedPrice() {
        Object highlight = cmbPrice.getProperties().get(HIGHLIGHT_KEY);
        if (cmbPrice.isShowing() && highlight instanceof Integer index
                && index >= 0 && index < cmbPrice.getItems().size()) {
            return cmbPrice.getItems().get(index);
        }
        return cmbPrice.getValue();
    }

    private ListView<?> getPopupListView() {
        if (cmbPrice.getSkin() instanceof ComboBoxListViewSkin<?> skin
                && skin.getPopupContent() instanceof ListView<?> listView) {
            return listView;
        }
        return null;
    }

    private static String formatPrice(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%,.0f", value);
        }
        return String.format(Locale.US, "%,.2f", value);
    }
}
