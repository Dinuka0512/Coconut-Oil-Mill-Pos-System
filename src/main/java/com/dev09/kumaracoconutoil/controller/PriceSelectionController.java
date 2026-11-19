package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.PriceQtyArray;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for the modal "Select Unit Price" popup defined in priceSelection.fxml.
 *
 * <p>Each product price carries its own stock quantity, so the list shows both, e.g.
 * {@code LKR 1,000   (5 Kg available)}, and the caller receives the selected
 * {@code {price, qty}} tier.</p>
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
    private ComboBox<Integer> cmbPrice;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSelect;

    private static final String HIGHLIGHT_KEY = "price.highlightIndex";
    private static final String NAV_KEY = "price.navInstalled";

    /** The product's {price, qty} rows; the combo stores indices into this array. */
    private double[][] rows = new double[0][];
    private double[] selectedTier;
    private boolean confirmed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbPrice.setEditable(false);
        cmbPrice.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer value) {
                return value == null ? "" : formatOption(value);
            }

            @Override
            public Integer fromString(String text) {
                return null;
            }
        });
        cmbPrice.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatOption(item));
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
    public void initData(String productName, double[][] priceQtyRows) {
        lblProduct.setText(productName == null ? "" : productName);
        rows = priceQtyRows == null ? new double[0][] : priceQtyRows;
        ObservableList<Integer> indices = FXCollections.observableArrayList();
        for (int i = 0; i < rows.length; i++) {
            indices.add(i);
        }
        cmbPrice.setItems(indices);
        if (!indices.isEmpty()) {
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

    /** The selected {@code {price, qty}} row, or null when nothing was confirmed. */
    public double[] getSelectedTier() {
        return selectedTier;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    void btnSelectOnAction(ActionEvent event) {
        confirm(getHighlightedIndex());
    }

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStage();
    }

    private void confirm(Integer index) {
        if (confirmed || index == null || index < 0 || index >= rows.length) {
            return;
        }
        confirmed = true;
        selectedTier = rows[index];
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
                confirm(getHighlightedIndex());
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
                    confirm(getHighlightedIndex());
                } else if (code == KeyCode.ESCAPE) {
                    event.consume();
                    cmbPrice.hide();
                }
            });

            // A single mouse click confirms immediately instead of requiring a second click.
            listView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                ListCell<?> cell = findCell(event);
                if (cell == null || cell.isEmpty() || !(cell.getItem() instanceof Integer index)) {
                    return;
                }
                event.consume();
                confirm(index);
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

    private Integer getHighlightedIndex() {
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

    private String formatOption(int index) {
        if (index < 0 || index >= rows.length) {
            return "";
        }
        double price = rows[index][PriceQtyArray.PRICE];
        double qty = rows[index][PriceQtyArray.QTY];
        return "LKR " + formatPrice(price) + "   (" + formatQty(qty) + " Kg available)";
    }

    private static String formatPrice(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%,.0f", value);
        }
        return String.format(Locale.US, "%,.2f", value);
    }

    private static String formatQty(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%,.0f", value);
        }
        return String.format(Locale.US, "%,.2f", value);
    }
}
