package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.util.PdfGenerator;
import com.dev09.kumaracoconutoil.util.ThermalPrinter;
import com.dev09.kumaracoconutoil.util.PriceQtyArray;
import com.dev09.kumaracoconutoil.util.PriceSelectionDialog;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuotationController implements Initializable {

    @FXML
    private AnchorPane body;

    @FXML
    private ComboBox<String> cmbCustomerId;

    @FXML
    private ComboBox<String> cmbProductId;

    @FXML
    private TableColumn<CartTM, String> colDescription;

    @FXML
    private TableColumn<CartTM, Integer> colQty;

    @FXML
    private TableColumn<CartTM, Double> colTotal;

    @FXML
    private TableColumn<CartTM, Double> colUnitPrice;

    @FXML
    private TableColumn<CartTM, Button> colAction;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private TextField txtUnitPrice;

    @FXML
    private TableView<CartTM> tblCart;

    @FXML
    private TextField txtQty;

    private CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);

    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private java.util.List<CustomerDto> currentCustomers;
    private java.util.List<ProductDto> currentProducts;
    private CustomerDto selectedCustomer;
    private ProductDto selectedProduct;
    /** Stock available at the currently selected price tier. */
    private double selectedTierQty;
    private String lastCommittedProductSelection = "";
    private long lastCommittedProductAt;
    private static int quotationCounter = 0;
    private String currentQuotationId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblCart.setItems(obList);

        loadCustomerIds();
        loadProductIds();
        setQuotationId();

        cmbProductId.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbProductId.getEditor() != null && cmbProductId.getEditor().getText().trim().isEmpty()) {
                repopulateAllProducts();
            }
        });
        cmbCustomerId.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbCustomerId.getEditor() != null && cmbCustomerId.getEditor().getText().trim().isEmpty()) {
                repopulateAllCustomers();
            }
        });

        installComboNavigation(cmbCustomerId);
        installComboNavigation(cmbProductId);
        installFocusFlowHandlers();

        txtQty.setOnAction(e -> txtUnitPrice.requestFocus());
        txtUnitPrice.setOnAction(e -> btnAddToCartOnAction(null));

        body.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() != KeyCode.ESCAPE) return;
            if (cmbCustomerId.isShowing() || cmbProductId.isShowing()) return;
            event.consume();
            handleEscape();
        });

        javafx.application.Platform.runLater(() -> {
            if (cmbProductId.getEditor() != null) {
                cmbProductId.getEditor().requestFocus();
            } else {
                cmbProductId.requestFocus();
            }
            javafx.scene.Scene scene = tblCart.getScene();
            if (scene != null) {
                scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
                    () -> {
                        if (body.getScene() == scene) {
                            btnGeneratePdfOnAction(null);
                        }
                    }
                );
            }
        });
    }

    /** Esc goes back directly when nothing was added, otherwise asks for confirmation. */
    private void handleEscape() {
        boolean hasValues = !obList.isEmpty() || selectedProduct != null;
        if (hasValues && !AlertUtil.confirmClose()) {
            return;
        }
        btnBackOnAction(null);
    }

    private void setQuotationId() {
        quotationCounter++;
        currentQuotationId = String.format("Q%03d", quotationCounter);
    }

    private boolean isNavigationKey(KeyEvent event) {
        if (event == null || event.getCode() == null) return false;
        switch (event.getCode()) {
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
            case ENTER:
            case ESCAPE:
            case TAB:
            case PAGE_UP:
            case PAGE_DOWN:
            case HOME:
            case END:
            case SHIFT:
            case CONTROL:
            case ALT:
                return true;
            default:
                return false;
        }
    }

    /** Same arrow-key dropdown behaviour as the POS/invoice product combo. */
    private void installComboNavigation(ComboBox<String> combo) {
        if (combo == null) return;

        combo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                Object hi = combo.getProperties().get("quote.highlightIndex");
                boolean highlighted = !empty && item != null && hi instanceof Integer && ((Integer) hi) == getIndex();
                setStyle(highlighted ? "-fx-background-color: #2563eb; -fx-text-fill: white;" : "");
            }
        });

        combo.showingProperty().addListener((obs, was, showing) -> {
            if (showing) {
                installPopupNavigation(combo, 0);
                Platform.runLater(() -> installPopupNavigation(combo, 1));
            } else {
                combo.getProperties().remove("quote.highlightIndex");
            }
        });

        javafx.event.EventHandler<KeyEvent> handler = event -> {
            if (event.getEventType() != KeyEvent.KEY_PRESSED) return;
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN || code == KeyCode.UP) {
                event.consume();
                if (!combo.isShowing()) {
                    if (combo.getItems().isEmpty()) {
                        return;
                    }
                    combo.show();
                }
                moveComboHighlight(combo, code == KeyCode.DOWN ? 1 : -1, 0);
            } else if (code == KeyCode.ENTER) {
                if (combo.isShowing()) {
                    String highlighted = getHighlightedComboItem(combo);
                    if (highlighted != null) {
                        event.consume();
                        commitComboSelection(combo, highlighted);
                    }
                }
            } else if (code == KeyCode.ESCAPE && combo.isShowing()) {
                combo.getProperties().remove("quote.highlightIndex");
                combo.hide();
                event.consume();
            }
        };
        combo.addEventFilter(KeyEvent.KEY_PRESSED, handler);
        if (combo.getEditor() != null) {
            combo.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, handler);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void installPopupNavigation(ComboBox<String> combo, int attempt) {
        ListView listView = getComboPopupListView(combo);
        if (listView == null) {
            if (attempt < 10) {
                Platform.runLater(() -> installPopupNavigation(combo, attempt + 1));
            }
            return;
        }
        if (Boolean.TRUE.equals(listView.getProperties().get("quote.navInstalled"))) return;
        listView.getProperties().put("quote.navInstalled", true);
        listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN || code == KeyCode.UP) {
                event.consume();
                moveComboHighlight(combo, code == KeyCode.DOWN ? 1 : -1, 0);
            } else if (code == KeyCode.PAGE_DOWN) {
                event.consume();
                moveComboHighlight(combo, 5, 0);
            } else if (code == KeyCode.PAGE_UP) {
                event.consume();
                moveComboHighlight(combo, -5, 0);
            } else if (code == KeyCode.HOME) {
                event.consume();
                combo.getProperties().put("quote.highlightIndex", 0);
                listView.refresh();
            } else if (code == KeyCode.END) {
                event.consume();
                if (!combo.getItems().isEmpty()) {
                    combo.getProperties().put("quote.highlightIndex", combo.getItems().size() - 1);
                    listView.refresh();
                }
            } else if (code == KeyCode.ENTER) {
                event.consume();
                if (!combo.isShowing()) return;
                String highlighted = getHighlightedComboItem(combo);
                if (highlighted != null) commitComboSelection(combo, highlighted);
            } else if (code == KeyCode.TAB && event.isShiftDown()) {
                // Key events can be redirected to the popup ListView while the dropdown is open.
                if (combo == cmbProductId) {
                    event.consume();
                    combo.hide();
                    focusComboEditor(cmbCustomerId);
                }
            } else if (code == KeyCode.ESCAPE) {
                event.consume();
                combo.getProperties().remove("quote.highlightIndex");
                combo.hide();
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void moveComboHighlight(ComboBox<String> combo, int delta, int attempt) {
        if (combo == null) return;
        ObservableList<String> items = combo.getItems();
        if (items == null || items.isEmpty()) {
            combo.getProperties().remove("quote.highlightIndex");
            return;
        }

        if (!combo.isShowing()) {
            combo.show();
        }

        ListView listView = getComboPopupListView(combo);
        if (listView == null) {
            if (attempt < 15) {
                Platform.runLater(() -> moveComboHighlight(combo, delta, attempt + 1));
            }
            return;
        }

        Object hi = combo.getProperties().get("quote.highlightIndex");
        int current = hi instanceof Integer ? (Integer) hi : -1;
        int size = items.size();
        if (current < 0 || current >= size) {
            current = delta > 0 ? -1 : size;
        }
        int target = current + delta;
        if (target < 0) target = 0;
        if (target >= size) target = size - 1;

        combo.getProperties().put("quote.highlightIndex", target);
        try {
            listView.refresh();
            listView.scrollTo(target);
        } catch (Exception ignored) {
        }
    }

    private void commitComboSelection(ComboBox<String> combo, String value) {
        if (combo == null || value == null) return;
        combo.getProperties().remove("quote.highlightIndex");
        combo.getProperties().put("quote.suppressFilter", true);
        try {
            if (combo.getSelectionModel() != null) {
                combo.getSelectionModel().select(value);
            }
            combo.setValue(value);
            if (combo.getEditor() != null) {
                combo.getEditor().setText(value);
                combo.getEditor().positionCaret(value.length());
            }
        } finally {
            combo.getProperties().put("quote.suppressFilter", false);
        }
        combo.hide();
        combo.fireEvent(new javafx.event.ActionEvent(combo, combo));
    }

    @SuppressWarnings({"rawtypes"})
    private String getHighlightedComboItem(ComboBox<String> combo) {
        ObservableList<String> items = combo.getItems();
        if (items == null || items.isEmpty()) return null;
        Object hi = combo.getProperties().get("quote.highlightIndex");
        int idx = hi instanceof Integer ? (Integer) hi : -1;
        if (idx >= 0 && idx < items.size()) return items.get(idx);
        return null;
    }

    @SuppressWarnings({"rawtypes"})
    private ListView getComboPopupListView(ComboBox<String> combo) {
        try {
            if (combo.getSkin() instanceof ComboBoxListViewSkin) {
                Node popupContent = ((ComboBoxListViewSkin) combo.getSkin()).getPopupContent();
                if (popupContent instanceof ListView) return (ListView) popupContent;
                if (popupContent != null) {
                    if (popupContent instanceof javafx.scene.Parent) {
                        Node n = popupContent.lookup(".list-view");
                        if (n instanceof ListView) return (ListView) n;
                    }
                    ListView found = findListView(popupContent);
                    if (found != null) return found;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            if (combo.getSkin() != null && combo.getSkin().getNode() != null) {
                Node skinNode = combo.getSkin().getNode();
                Node lv = skinNode.lookup(".list-view");
                if (lv instanceof ListView) return (ListView) lv;
                ListView found = findListView(skinNode);
                if (found != null) return found;
            }
        } catch (Exception ignored) {
        }
        try {
            Node n = combo.lookup(".list-view");
            if (n instanceof ListView) return (ListView) n;
        } catch (Exception ignored) {
        }
        try {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (!w.isShowing()) continue;
                javafx.scene.Scene sc = null;
                if (w instanceof javafx.stage.Stage) sc = ((javafx.stage.Stage) w).getScene();
                else if (w instanceof javafx.stage.PopupWindow) sc = ((javafx.stage.PopupWindow) w).getScene();
                if (sc != null && sc.getRoot() != null) {
                    Node lv = sc.getRoot().lookup(".list-view");
                    if (lv instanceof ListView) return (ListView) lv;
                    ListView found = findListView(sc.getRoot());
                    if (found != null) return found;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings({"rawtypes"})
    private ListView findListView(Node node) {
        if (node == null) return null;
        if (node instanceof ListView) return (ListView) node;
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                ListView found = findListView(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void comboSetItems(ComboBox<String> combo, java.util.List<String> items) {
        if (combo == null) return;
        java.util.List<String> newItems = items == null ? java.util.Collections.emptyList() : items;
        if (newItems.isEmpty() && combo.isShowing()) {
            combo.hide();
        }
        combo.getProperties().put("quote.suppressFilter", true);
        try {
            ObservableList<String> comboItems = combo.getItems();
            if (comboItems != null) {
                comboItems.setAll(newItems);
            }
        } finally {
            combo.getProperties().put("quote.suppressFilter", false);
        }
        combo.getProperties().remove("quote.highlightIndex");
    }

    private void showComboIfHasItems(ComboBox<String> combo) {
        if (combo.getItems().isEmpty()) {
            combo.hide();
        } else {
            combo.show();
        }
    }

    /** POS-style Shift+Tab and customer-to-product focus flow. */
    private void installFocusFlowHandlers() {
        javafx.event.EventHandler<KeyEvent> productShiftTab = event -> {
            if (event.getCode() == KeyCode.TAB && event.isShiftDown()) {
                event.consume();
                focusComboEditor(cmbCustomerId);
            }
        };
        cmbProductId.addEventFilter(KeyEvent.KEY_PRESSED, productShiftTab);
        if (cmbProductId.getEditor() != null) {
            cmbProductId.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, productShiftTab);
        }

        javafx.event.EventHandler<KeyEvent> customerToProduct = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                Platform.runLater(() -> {
                    if (selectedCustomer != null) {
                        focusComboEditor(cmbProductId);
                    }
                });
            }
        };
        cmbCustomerId.addEventFilter(KeyEvent.KEY_PRESSED, customerToProduct);
        if (cmbCustomerId.getEditor() != null) {
            cmbCustomerId.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, customerToProduct);
        }
    }

    private void focusComboEditor(ComboBox<String> combo) {
        if (combo == null) return;
        if (combo.getEditor() != null) {
            combo.getEditor().requestFocus();
        } else {
            combo.requestFocus();
        }
    }

    private void loadCustomerIds() {
        currentCustomers = customerBO.getAllCustomers();
        int index = 0;
        for (CustomerDto dto : currentCustomers) {
            cmbCustomerId.getItems().add(dto.getName() + " - " + dto.getContact());
            if (AppConstants.DEMO_CUSTOMER_ID.equals(dto.getCustId())) {
                selectedCustomer = dto;
                cmbCustomerId.getSelectionModel().select(index);
            }
            index++;
        }
    }

    private void loadProductIds() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProductId.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
        }
    }

    @FXML
    void btnNewCustomerOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Customer");
            stage.setResizable(false);
            stage.showAndWait();

            selectedCustomer = null;
            comboSetItems(cmbCustomerId, java.util.Collections.emptyList());
            cmbCustomerId.getSelectionModel().clearSelection();
            if (cmbCustomerId.getEditor() != null) cmbCustomerId.getEditor().clear();
            loadCustomerIds();
            focusComboEditor(cmbCustomerId);
            if (!cmbCustomerId.getItems().isEmpty()) {
                cmbCustomerId.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage());
        }
    }

    @FXML
    void btnNewProductOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/inventory.fxml"));
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("New Inventory");
            stage.setResizable(false);
            stage.showAndWait();

            selectedProduct = null;
            selectedTierQty = 0;
            lastCommittedProductSelection = null;
            lastCommittedProductAt = 0;
            comboSetItems(cmbProductId, java.util.Collections.emptyList());
            cmbProductId.getSelectionModel().clearSelection();
            if (cmbProductId.getEditor() != null) cmbProductId.getEditor().clear();
            txtUnitPrice.clear();
            lblQtyOnHand.setText("0Kg");
            loadProductIds();
            focusComboEditor(cmbProductId);
            if (!cmbProductId.getItems().isEmpty()) {
                cmbProductId.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage());
        }
    }

    @FXML
    void cmbCustomerOnAction(ActionEvent event) {
        resolveSelectedCustomer();
        if (selectedCustomer != null) {
            String display = customerDisplay(selectedCustomer);
            cmbCustomerId.getProperties().put("quote.suppressFilter", true);
            try {
                cmbCustomerId.setValue(display);
                if (cmbCustomerId.getEditor() != null) {
                    cmbCustomerId.getEditor().setText(display);
                    cmbCustomerId.getEditor().positionCaret(display.length());
                }
            } finally {
                cmbCustomerId.getProperties().put("quote.suppressFilter", false);
            }
            Platform.runLater(() -> focusComboEditor(cmbProductId));
        }
    }

    /** Same exact/unique-partial customer matching as the POS customer dropdown. */
    private void resolveSelectedCustomer() {
        if (currentCustomers == null) {
            selectedCustomer = null;
            return;
        }

        String selection = cmbCustomerId.getValue();
        String editorText = cmbCustomerId.getEditor() != null ? cmbCustomerId.getEditor().getText() : null;
        java.util.ArrayList<String> candidates = new java.util.ArrayList<>();
        if (editorText != null && !editorText.trim().isEmpty()) candidates.add(editorText.trim());
        if (selection != null && !selection.trim().isEmpty() && !candidates.contains(selection.trim())) {
            candidates.add(selection.trim());
        }

        for (String candidate : candidates) {
            CustomerDto exact = findExactCustomerMatch(candidate);
            if (exact != null) {
                selectedCustomer = exact;
                return;
            }
        }
        for (String candidate : candidates) {
            CustomerDto unique = findUniquePartialCustomerMatch(candidate);
            if (unique != null) {
                selectedCustomer = unique;
                return;
            }
        }
        selectedCustomer = null;
    }

    private String customerDisplay(CustomerDto dto) {
        String name = dto.getName() != null ? dto.getName() : "";
        String contact = dto.getContact() != null ? dto.getContact() : "";
        return name + " - " + contact;
    }

    private CustomerDto findExactCustomerMatch(String candidate) {
        for (CustomerDto dto : currentCustomers) {
            if (customerDisplay(dto).equalsIgnoreCase(candidate)) return dto;
        }
        for (CustomerDto dto : currentCustomers) {
            if (dto.getName() != null && dto.getName().equalsIgnoreCase(candidate)) return dto;
        }
        for (CustomerDto dto : currentCustomers) {
            if (dto.getContact() != null && dto.getContact().equalsIgnoreCase(candidate)) return dto;
        }
        return null;
    }

    private CustomerDto findUniquePartialCustomerMatch(String candidate) {
        String c = candidate.toLowerCase();
        CustomerDto found = null;
        for (CustomerDto dto : currentCustomers) {
            String display = customerDisplay(dto).toLowerCase();
            String name = dto.getName() != null ? dto.getName().toLowerCase() : "";
            String contact = dto.getContact() != null ? dto.getContact().toLowerCase() : "";
            boolean matches = display.contains(c) || name.contains(c) || contact.contains(c);
            if (matches) {
                if (found != null && found != dto) {
                    return null;
                }
                found = dto;
            }
        }
        return found;
    }

    private Timeline custResetTimer;

    @FXML
    void cmbCustomerOnKeyReleased(javafx.scene.input.KeyEvent event) {
        if (isNavigationKey(event)) return;
        if (Boolean.TRUE.equals(cmbCustomerId.getProperties().get("quote.suppressFilter"))) return;
        cmbCustomerId.getProperties().remove("quote.highlightIndex");
        String searchText = cmbCustomerId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllCustomers();
            if (custResetTimer != null) custResetTimer.stop();
            showComboIfHasItems(cmbCustomerId);
        } else {
            java.util.List<String> matches = new java.util.ArrayList<>();
            for (CustomerDto dto : currentCustomers) {
                String display = dto.getName() + " - " + dto.getContact();
                if (dto.getName().toLowerCase().contains(searchText) ||
                    dto.getContact().toLowerCase().contains(searchText)) {
                    matches.add(display);
                }
            }
            comboSetItems(cmbCustomerId, matches);
            showComboIfHasItems(cmbCustomerId);

            if (custResetTimer != null) custResetTimer.stop();
            custResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbCustomerId.getItems().isEmpty()) {
                    cmbCustomerId.getEditor().setText("");
                    repopulateAllCustomers();
                }
            }));
            custResetTimer.setCycleCount(1);
            custResetTimer.play();
        }
    }

    @FXML
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProductId.getValue();
        if ((selection == null || selection.trim().isEmpty()) && cmbProductId.getEditor() != null) {
            selection = cmbProductId.getEditor().getText();
        }
        String sel = selection == null ? "" : selection.trim();

        // One commit can surface as multiple ActionEvents; only handle the first one so
        // the price selection popup is not opened twice (same guard as the POS invoice UI).
        boolean duplicate = sel.equals(lastCommittedProductSelection)
                && System.currentTimeMillis() - lastCommittedProductAt < 3000;
        if (!duplicate) {
            lastCommittedProductSelection = sel;
            lastCommittedProductAt = System.currentTimeMillis();
        }
        if (duplicate) {
            return;
        }

        if (currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (display.equalsIgnoreCase(sel) || dto.getName().equalsIgnoreCase(sel)) {
                    double[] tier = resolveProductTier(dto);
                    if (tier == null) {
                        resetProductSelection();
                        return;
                    }
                    selectedProduct = dto;
                    selectedTierQty = tier[PriceQtyArray.QTY];
                    cmbProductId.setValue(display);
                    if (cmbProductId.getEditor() != null) {
                        cmbProductId.getEditor().setText(display);
                        cmbProductId.getEditor().positionCaret(display.length());
                    }
                    txtUnitPrice.setText(String.valueOf(tier[PriceQtyArray.PRICE]));
                    lblQtyOnHand.setText(selectedTierQty + " Kg");
                    txtQty.requestFocus();
                    break;
                }
            }
        }
    }

    /** The {price, qty} tier the cashier selected, or null when cancelled. */
    private double[] resolveProductTier(ProductDto dto) {
        double[][] rows = dto.getPriceQtyPairs();

        if (rows.length == 0) {
            return new double[]{dto.getPrice(), dto.getQty()};
        }
        if (rows.length == 1) {
            return rows[0];
        }

        cmbProductId.hide();
        Window owner = cmbProductId.getScene() == null ? null : cmbProductId.getScene().getWindow();
        return PriceSelectionDialog.show(owner, dto.getName(), rows).orElse(null);
    }

    private void resetProductSelection() {
        selectedProduct = null;
        selectedTierQty = 0;
        lastCommittedProductSelection = null;
        lastCommittedProductAt = 0;
        cmbProductId.getSelectionModel().clearSelection();
        if (cmbProductId.getEditor() != null) {
            cmbProductId.getEditor().clear();
        }
        txtUnitPrice.clear();
        lblQtyOnHand.setText("");
        focusComboEditor(cmbProductId);
    }

    private Timeline resetTimer;

    @FXML
    void cmbProductOnKeyReleased(javafx.scene.input.KeyEvent event) {
        if (isNavigationKey(event)) return;
        if (Boolean.TRUE.equals(cmbProductId.getProperties().get("quote.suppressFilter"))) return;
        cmbProductId.getProperties().remove("quote.highlightIndex");
        String searchText = cmbProductId.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllProducts();
            if (resetTimer != null) resetTimer.stop();
            showComboIfHasItems(cmbProductId);
        } else {
            java.util.List<String> matches = new java.util.ArrayList<>();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (dto.getName().toLowerCase().contains(searchText) ||
                    (dto.getItemcode() != null ? dto.getItemcode() : "").toLowerCase().contains(searchText)) {
                    matches.add(display);
                }
            }
            comboSetItems(cmbProductId, matches);
            showComboIfHasItems(cmbProductId);

            if (resetTimer != null) resetTimer.stop();
            resetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProductId.getItems().isEmpty()) {
                    cmbProductId.getEditor().setText("");
                    repopulateAllProducts();
                }
            }));
            resetTimer.setCycleCount(1);
            resetTimer.play();
        }
    }

    @FXML
    void btnAddToCartOnAction(ActionEvent event) {
        if (selectedProduct == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select an inventory first!");
            return;
        }
        String code = selectedProduct.getId();
        String description = selectedProduct.getName();
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText());
        } catch (NumberFormatException ex) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please enter a valid quantity!");
            return;
        }

        if (qty <= 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Quantity must be greater than zero!");
            return;
        }

        try {
            String priceStr = txtUnitPrice.getText();
            double unitPrice = Double.parseDouble(priceStr.trim());
            if (unitPrice <= 0) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Unit price must be greater than zero!");
                return;
            }

            // Stock is tracked per price tier, so the entered quantity is limited to the
            // stock of the selected price (not the product's total stock).
            double existingQtyInCart = 0;
            for (CartTM item : obList) {
                if (code.equals(item.getCode()) && unitPrice == item.getUnitPrice()) {
                    existingQtyInCart = item.getQty();
                    break;
                }
            }
            if (existingQtyInCart + qty > selectedTierQty) {
                AlertUtil.showAlert(Alert.AlertType.WARNING,
                        "Quantity exceeds the stock available for this price!\nAvailable: "
                                + selectedTierQty + " Kg\nIn Cart: " + existingQtyInCart + " Kg");
                return;
            }

            double total = qty * unitPrice;
            Button btn = new Button("Remove");
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

            btn.setOnAction(e -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to remove?", yes, no).showAndWait();

                if (result.orElse(no) == yes) {
                    obList.removeIf(item -> item.getBtn() == btn);
                    calculateTotal();
                }
            });

            boolean merged = false;
            for (CartTM item : obList) {
                if (code.equals(item.getCode()) && unitPrice == item.getUnitPrice()) {
                    item.setQty(item.getQty() + qty);
                    item.setTotal(item.getTotal() + total);
                    tblCart.refresh();
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                obList.add(new CartTM(code, description, qty, unitPrice, total, btn));
            }
            calculateTotal();
            clearProductSelection();
            focusComboEditor(cmbProductId);
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid unit price!");
        }
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }
        lblNetTotal.setText(String.format("Total: Rs. %.2f", netTotal));
    }

    @FXML
    void btnGeneratePdfOnAction(ActionEvent event) {
        if (obList.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }

        String quotationId = currentQuotationId;
        LocalDate date = LocalDate.now();

        String customerName = selectedCustomer != null ? selectedCustomer.getName() : "-";
        String customerContact = selectedCustomer != null ? selectedCustomer.getContact() : null;

        Alert printAlert = new Alert(Alert.AlertType.CONFIRMATION);
        printAlert.setTitle("Generate Quotation");
        printAlert.setHeaderText("Choose output format:");
        printAlert.setContentText("Select the format for the quotation:");

        ButtonType smallBtn = new ButtonType("Small (80mm) - Thermal Printer", ButtonBar.ButtonData.YES);
        ButtonType a4Btn = new ButtonType("A4 PDF", ButtonBar.ButtonData.NO);
        printAlert.getButtonTypes().setAll(smallBtn, a4Btn);

        Optional<ButtonType> result = printAlert.showAndWait();
        if (result.isPresent()) {
            try {
                if (result.get() == smallBtn) {
                    byte[] escpos = PdfGenerator.generateQuotationBill80mm(
                            quotationId, date, customerName, customerContact,
                            new ArrayList<>(obList), 0, 0);
                    ThermalPrinter.print(escpos);
                } else {
                    byte[] pdfBytes = PdfGenerator.generateQuotationBillA4Pro(
                            quotationId, date, customerName, customerContact,
                            new ArrayList<>(obList), 0, 0);
                    File tempFile = File.createTempFile("Quotation_" + quotationId, ".pdf");
                    tempFile.deleteOnExit();
                    Files.write(tempFile.toPath(), pdfBytes);
                    Desktop.getDesktop().open(tempFile);
                }
                resetForm();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error generating PDF: " + e.getMessage());
            }
        }
    }

    private void repopulateAllProducts() {
        java.util.List<String> displays = new ArrayList<>();
        if (currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                displays.add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
        }
        comboSetItems(cmbProductId, displays);
    }

    private void repopulateAllCustomers() {
        java.util.List<String> displays = new ArrayList<>();
        if (currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                displays.add(dto.getName() + " - " + dto.getContact());
            }
        }
        comboSetItems(cmbCustomerId, displays);
    }

    private void clearProductSelection() {
        selectedProduct = null;
        selectedTierQty = 0;
        txtQty.clear();
        txtQty.setText("1");
        txtUnitPrice.clear();
        lblQtyOnHand.setText("0Kg");
        cmbProductId.getEditor().clear();
        cmbProductId.getSelectionModel().clearSelection();
        repopulateAllProducts();
    }

    private void resetForm() {
        obList.clear();
        tblCart.refresh();

        comboSetItems(cmbCustomerId, java.util.Collections.emptyList());
        cmbCustomerId.getSelectionModel().clearSelection();
        if (cmbCustomerId.getEditor() != null) cmbCustomerId.getEditor().clear();
        loadCustomerIds();
        selectedCustomer = null;

        clearProductSelection();

        lblNetTotal.setText("Total: Rs. 0.00");
    }

    @FXML
    void btnBackOnAction(ActionEvent event) {
        try {
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
            if (body != null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) body.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
