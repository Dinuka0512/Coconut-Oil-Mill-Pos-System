package com.dev09.kumaracoconutoil.controller;

import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.util.AlertUtil;
import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.view.tm.CartTM;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class SimplePOSController implements Initializable {

    @FXML
    private ComboBox<String> cmbProduct;

    @FXML
    private ComboBox<String> cmbCustomer;

    @FXML
    private TextField txtQty;

    @FXML
    private TextField txtUnitPriceDisplay;

    @FXML
    private Label lblQtyOnHand;

    @FXML
    private Label lblNetTotal;

    @FXML
    private Label lblDueWarning;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblItemCode;

    @FXML
    private TableView<CartTM> tblCart;

    @FXML
    private TableColumn<CartTM, Integer> colIndex;

    @FXML
    private TableColumn<CartTM, String> colProduct;

    @FXML
    private TableColumn<CartTM, Double> colQty;

    @FXML
    private TableColumn<CartTM, Double> colPrice;

    @FXML
    private TableColumn<CartTM, Double> colTotal;

    @FXML
    private TableColumn<CartTM, Button> colAction;

    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private final OrderBO orderBO = (OrderBO) BOFactory.getInstance().getBo(BOFactory.BOType.ORDER);

    private ObservableList<CartTM> obList = FXCollections.observableArrayList();
    private ArrayList<ProductDto> currentProducts;
    private ArrayList<CustomerDto> currentCustomers;
    private ProductDto selectedProduct;
    private CustomerDto selectedCustomer;
    private Timeline productResetTimer;
    private Timeline customerResetTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colIndex.setCellFactory(col -> new TableCell<CartTM, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        colProduct.setCellValueFactory(new PropertyValueFactory<>("description"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));

        tblCart.setItems(obList);
        loadProducts();
        loadCustomers();

        txtUnitPriceDisplay.setEditable(true);

        txtQty.setOnAction(e -> {
            if (selectedProduct == null) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Select a product first!");
                cmbProduct.requestFocus();
                return;
            }
            try {
                double qty = Double.parseDouble(txtQty.getText());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter a valid quantity!");
                txtQty.requestFocus();
                return;
            }
            txtUnitPriceDisplay.requestFocus();
        });

        txtUnitPriceDisplay.setOnAction(e -> btnAddToCartOnAction(null));

        installComboNavigation(cmbProduct);
        installComboNavigation(cmbCustomer);

        cmbProduct.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbProduct.getEditor() != null && cmbProduct.getEditor().getText().trim().isEmpty()) {
                repopulateAllProducts();
            }
        });
        cmbCustomer.showingProperty().addListener((obs, was, showing) -> {
            if (showing && cmbCustomer.getEditor() != null && cmbCustomer.getEditor().getText().trim().isEmpty()) {
                repopulateAllCustomers();
            }
        });

        javafx.application.Platform.runLater(() -> {
            if (cmbProduct.getEditor() != null) {
                cmbProduct.getEditor().requestFocus();
            } else {
                cmbProduct.requestFocus();
            }
        });

        installFocusFlowHandlers();

        javafx.application.Platform.runLater(() -> {
            javafx.scene.Scene scene = tblCart.getScene();
            if (scene != null) {
                scene.getAccelerators().put(
                    new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.ENTER, javafx.scene.input.KeyCombination.CONTROL_DOWN),
                    () -> btnPlaceOrderOnAction(null)
                );
            }
        });
    }

    private void loadProducts() {
        currentProducts = productBO.getAllProducts();
        for (ProductDto dto : currentProducts) {
            cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
        }
    }

    private void loadCustomers() {
        currentCustomers = customerBO.getAllCustomers();
        int index = 0;
        for (CustomerDto dto : currentCustomers) {
            cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
            if (AppConstants.DEMO_CUSTOMER_ID.equals(dto.getCustId())) {
                selectedCustomer = dto;
                cmbCustomer.getSelectionModel().select(index);
            }
            index++;
        }
        checkCustomerDue();
    }

    @FXML
    void cmbProductOnAction(ActionEvent event) {
        String selection = cmbProduct.getValue();
        if ((selection == null || selection.trim().isEmpty()) && cmbProduct.getEditor() != null) {
            selection = cmbProduct.getEditor().getText();
        }
        if (selection != null && currentProducts != null) {
            String sel = selection.trim();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (display.equalsIgnoreCase(sel) || dto.getName().equalsIgnoreCase(sel)) {
                    selectedProduct = dto;
                    cmbProduct.setValue(display);
                    if (cmbProduct.getEditor() != null) {
                        cmbProduct.getEditor().setText(display);
                        cmbProduct.getEditor().positionCaret(display.length());
                    }
                    txtUnitPriceDisplay.setText(String.valueOf(dto.getPrice()));
                    lblQtyOnHand.setText(dto.getQty() + " Kg");
                    lblItemName.setText(dto.getName());
                    lblItemCode.setText(dto.getItemcode() != null ? dto.getItemcode() : "-");
                    txtQty.requestFocus();
                    break;
                }
            }
        }
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

    private void installComboNavigation(ComboBox<String> combo) {
        if (combo == null) return;

        // Highlight is driven purely by a tracked index, never by the ListView selection model.
        // This avoids the ComboBox skin syncing a browswed item into value/editor (which caused "commit on arrow").
        combo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                Object hi = combo.getProperties().get("pos.highlightIndex");
                boolean highlighted = !empty && item != null && hi instanceof Integer && ((Integer) hi) == getIndex();
                setStyle(highlighted ? "-fx-background-color: #2563eb; -fx-text-fill: white;" : "");
            }
        });

        combo.showingProperty().addListener((obs, was, showing) -> {
            if (showing) {
                Platform.runLater(() -> installPopupNavigation(combo, 0));
            } else {
                combo.getProperties().remove("pos.highlightIndex");
            }
        });

        javafx.event.EventHandler<KeyEvent> handler = event -> {
            if (event.getEventType() != KeyEvent.KEY_PRESSED) return;
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN || code == KeyCode.UP) {
                event.consume();
                if (!combo.isShowing()) {
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
                combo.getProperties().remove("pos.highlightIndex");
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
        if (Boolean.TRUE.equals(listView.getProperties().get("pos.navInstalled"))) return;
        listView.getProperties().put("pos.navInstalled", true);
        listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN || code == KeyCode.UP) {
                event.consume();
                moveComboHighlight(combo, code == KeyCode.DOWN ? 1 : -1, 0);
            } else if (code == KeyCode.ENTER) {
                event.consume();
                if (!combo.isShowing()) return;
                String highlighted = getHighlightedComboItem(combo);
                if (highlighted != null) commitComboSelection(combo, highlighted);
            } else if (code == KeyCode.ESCAPE) {
                event.consume();
                combo.getProperties().remove("pos.highlightIndex");
                combo.hide();
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void moveComboHighlight(ComboBox<String> combo, int delta, int attempt) {
        if (combo == null) return;
        ObservableList<String> items = combo.getItems();
        if (items == null || items.isEmpty()) {
            combo.getProperties().remove("pos.highlightIndex");
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

        Object hi = combo.getProperties().get("pos.highlightIndex");
        int current = hi instanceof Integer ? (Integer) hi : -1;
        int size = items.size();
        if (current < 0 || current >= size) {
            current = delta > 0 ? -1 : size;
        }
        int target = current + delta;
        if (target < 0) target = 0;
        if (target >= size) target = size - 1;

        combo.getProperties().put("pos.highlightIndex", target);

        // Never touch the selection model here (that causes value/editor sync + accidental commit).
        try {
            listView.refresh();
            listView.scrollTo(target);
        } catch (Exception ignored) {
        }
    }

    private void commitComboSelection(ComboBox<String> combo, String value) {
        if (combo == null || value == null) return;
        combo.getProperties().remove("pos.highlightIndex");
        combo.getProperties().put("pos.suppressFilter", true);
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
            combo.getProperties().put("pos.suppressFilter", false);
        }
        combo.hide();
        combo.fireEvent(new javafx.event.ActionEvent(combo, combo));
    }

    @SuppressWarnings({"rawtypes"})
    private String getHighlightedComboItem(ComboBox<String> combo) {
        ObservableList<String> items = combo.getItems();
        if (items == null || items.isEmpty()) return null;
        Object hi = combo.getProperties().get("pos.highlightIndex");
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
        // Last resort: search popup windows' scenes (popup may live in separate window)
        try {
            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (!w.isShowing()) continue;
                if (w instanceof javafx.stage.PopupWindow || w instanceof javafx.stage.Stage) {
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
        combo.getProperties().put("pos.suppressFilter", true);
        try {
            ObservableList<String> comboItems = combo.getItems();
            if (comboItems != null) {
                comboItems.setAll(items == null ? java.util.Collections.emptyList() : items);
            }
        } finally {
            combo.getProperties().put("pos.suppressFilter", false);
        }
        // New result set: start arrow navigation from the top
        combo.getProperties().remove("pos.highlightIndex");
    }

    @FXML
    void cmbProductOnKeyReleased(KeyEvent event) {
        if (isNavigationKey(event)) return;
        if (Boolean.TRUE.equals(cmbProduct.getProperties().get("pos.suppressFilter"))) return;
        cmbProduct.getProperties().remove("pos.highlightIndex");
        String searchText = cmbProduct.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllProducts();
            if (productResetTimer != null) productResetTimer.stop();
            cmbProduct.show();
        } else {
            java.util.List<String> matches = new java.util.ArrayList<>();
            for (ProductDto dto : currentProducts) {
                String display = dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")";
                if (dto.getName().toLowerCase().contains(searchText) ||
                    (dto.getItemcode() != null ? dto.getItemcode() : "").toLowerCase().contains(searchText)) {
                    matches.add(display);
                }
            }
            comboSetItems(cmbProduct, matches);
            cmbProduct.show();

            if (productResetTimer != null) productResetTimer.stop();
            productResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbProduct.getItems().isEmpty()) {
                    cmbProduct.getEditor().setText("");
                    repopulateAllProducts();
                }
            }));
            productResetTimer.setCycleCount(1);
            productResetTimer.play();
        }
    }

    @FXML
    void cmbCustomerOnAction(ActionEvent event) {
        resolveSelectedCustomer();
        if (selectedCustomer != null) {
            String display = selectedCustomer.getName() + " - " + (selectedCustomer.getContact() != null ? selectedCustomer.getContact() : "");
            cmbCustomer.setValue(display);
            if (cmbCustomer.getEditor() != null) {
                cmbCustomer.getEditor().setText(display);
                cmbCustomer.getEditor().positionCaret(display.length());
            }
            javafx.application.Platform.runLater(() -> {
                if (cmbProduct.getEditor() != null) {
                    cmbProduct.getEditor().requestFocus();
                } else {
                    cmbProduct.requestFocus();
                }
            });
        }
    }

    private void resolveSelectedCustomer() {
        if (currentCustomers == null) {
            selectedCustomer = null;
            checkCustomerDue();
            return;
        }

        String selection = cmbCustomer.getValue();
        String editorText = cmbCustomer.getEditor() != null ? cmbCustomer.getEditor().getText() : null;

        java.util.ArrayList<String> candidates = new java.util.ArrayList<>();
        if (editorText != null && !editorText.trim().isEmpty()) candidates.add(editorText.trim());
        if (selection != null && !selection.trim().isEmpty() && !candidates.contains(selection.trim())) candidates.add(selection.trim());

        if (candidates.isEmpty()) {
            selectedCustomer = null;
            checkCustomerDue();
            return;
        }

        for (String candidate : candidates) {
            CustomerDto exact = findExactCustomerMatch(candidate);
            if (exact != null) {
                selectedCustomer = exact;
                checkCustomerDue();
                return;
            }
        }

        for (String candidate : candidates) {
            CustomerDto unique = findUniquePartialCustomerMatch(candidate);
            if (unique != null) {
                selectedCustomer = unique;
                if (cmbCustomer.getEditor() != null) {
                    cmbCustomer.getEditor().setText(unique.getName() + " - " + (unique.getContact() != null ? unique.getContact() : ""));
                }
                checkCustomerDue();
                return;
            }
        }

        selectedCustomer = null;
        checkCustomerDue();
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
                if (found != null && !found.getCustId().equals(dto.getCustId())) {
                    return null;
                }
                found = dto;
            }
        }
        return found;
    }

    private void checkCustomerDue() {
        if (selectedCustomer != null) {
            double due = selectedCustomer.getDueAmount();
            if (due > 0) {
                lblDueWarning.setText("⚠ Due: Rs. " + String.format("%.2f", due));
                lblDueWarning.setVisible(true);
            } else {
                lblDueWarning.setVisible(false);
            }
        }
    }

    private void installFocusFlowHandlers() {
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> productShiftTab = event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.TAB && event.isShiftDown()) {
                event.consume();
                if (cmbCustomer.getEditor() != null) {
                    cmbCustomer.getEditor().requestFocus();
                } else {
                    cmbCustomer.requestFocus();
                }
            }
        };
        cmbProduct.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, productShiftTab);
        if (cmbProduct.getEditor() != null) {
            cmbProduct.getEditor().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, productShiftTab);
        }

        javafx.event.EventHandler<javafx.scene.input.KeyEvent> customerToProduct = event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                // let selection handlers resolve; then move to product if a customer is selected
                javafx.application.Platform.runLater(() -> {
                    if (selectedCustomer != null) {
                        if (cmbProduct.getEditor() != null) {
                            cmbProduct.getEditor().requestFocus();
                        } else {
                            cmbProduct.requestFocus();
                        }
                    }
                });
            }
        };
        cmbCustomer.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, customerToProduct);
        if (cmbCustomer.getEditor() != null) {
            cmbCustomer.getEditor().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, customerToProduct);
        }
    }

    @FXML
    void cmbCustomerOnKeyReleased(KeyEvent event) {
        if (isNavigationKey(event)) return;
        if (Boolean.TRUE.equals(cmbCustomer.getProperties().get("pos.suppressFilter"))) return;
        cmbCustomer.getProperties().remove("pos.highlightIndex");
        String searchText = cmbCustomer.getEditor().getText().toLowerCase();
        if (searchText.isEmpty()) {
            repopulateAllCustomers();
            if (customerResetTimer != null) customerResetTimer.stop();
            cmbCustomer.show();
        } else {
            java.util.List<String> matches = new java.util.ArrayList<>();
            for (CustomerDto dto : currentCustomers) {
                String display = dto.getName() + " - " + dto.getContact();
                if (dto.getName().toLowerCase().contains(searchText) ||
                    dto.getContact().toLowerCase().contains(searchText)) {
                    matches.add(display);
                }
            }
            comboSetItems(cmbCustomer, matches);
            cmbCustomer.show();

            if (customerResetTimer != null) customerResetTimer.stop();
            customerResetTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                if (cmbCustomer.getItems().isEmpty()) {
                    cmbCustomer.getEditor().setText("");
                    repopulateAllCustomers();
                }
            }));
            customerResetTimer.setCycleCount(1);
            customerResetTimer.play();
        }
    }

    @FXML
    void btnNewProductOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/inventory.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("New Inventory");
            stage.setResizable(false);
            stage.showAndWait();

            selectedProduct = null;
            cmbProduct.getItems().clear();
            cmbProduct.getSelectionModel().clearSelection();
            if (cmbProduct.getEditor() != null) cmbProduct.getEditor().clear();
            loadProducts();
            if (cmbProduct.getEditor() != null) {
                cmbProduct.getEditor().requestFocus();
            } else {
                cmbProduct.requestFocus();
            }
            if (!cmbProduct.getItems().isEmpty()) {
                cmbProduct.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Inventory UI: " + e.getMessage());
        }
    }

    @FXML
    void btnNewCustomerOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/customers.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("New Customer");
            stage.setResizable(false);
            stage.showAndWait();

            CustomerDto previous = selectedCustomer;
            selectedCustomer = null;
            cmbCustomer.getItems().clear();
            cmbCustomer.getSelectionModel().clearSelection();
            if (cmbCustomer.getEditor() != null) cmbCustomer.getEditor().clear();
            loadCustomers();

            // If demo/previous auto-selection happened in loadCustomers but user likely wants to pick newly added one,
            // keep list ready for selection and focus the field.
            if (cmbCustomer.getEditor() != null) {
                cmbCustomer.getEditor().requestFocus();
            } else {
                cmbCustomer.requestFocus();
            }
            if (!cmbCustomer.getItems().isEmpty()) {
                cmbCustomer.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed to load Customers UI: " + e.getMessage());
        }
    }

    @FXML
    void btnAddToCartOnAction(ActionEvent event) {
        if (selectedProduct == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a product first!");
            return;
        }

        String code = selectedProduct.getId();
        String description = selectedProduct.getName();
        double qty;
        try {
            qty = Double.parseDouble(txtQty.getText());
        } catch (NumberFormatException ex) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Enter a valid quantity!");
            return;
        }

        if (qty <= 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Quantity must be greater than zero!");
            return;
        }

        double qtyOnHand = selectedProduct.getQty();
        double existingQtyInCart = 0;
        for (CartTM item : obList) {
            if (code.equals(item.getCode())) {
                existingQtyInCart = item.getQty();
                break;
            }
        }

        double totalQtyInCart = existingQtyInCart + qty;
        if (totalQtyInCart > qtyOnHand) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Total quantity exceeds available stock!\nAvailable: " + qtyOnHand + " Kg\nIn Cart: " + existingQtyInCart + " Kg");
            return;
        }

        try {
            double unitPrice = Double.parseDouble(txtUnitPriceDisplay.getText().trim());
            if (unitPrice <= 0) {
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Unit price must be greater than zero!");
                return;
            }
            double total = qty * unitPrice;
            Button btn = new Button("X");
            btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");

            btn.setOnAction(e -> {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "Remove this item?", yes, no).showAndWait();
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
            cmbProduct.requestFocus();
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Invalid unit price!");
        }
    }

    private void repopulateAllProducts() {
        if (cmbProduct.getItems() == null) return;
        cmbProduct.getItems().clear();
        if (currentProducts != null) {
            for (ProductDto dto : currentProducts) {
                cmbProduct.getItems().add(dto.getName() + " (" + (dto.getItemcode() != null ? dto.getItemcode() : "") + ")");
            }
        }
    }

    private void repopulateAllCustomers() {
        if (cmbCustomer.getItems() == null) return;
        cmbCustomer.getItems().clear();
        if (currentCustomers != null) {
            for (CustomerDto dto : currentCustomers) {
                cmbCustomer.getItems().add(dto.getName() + " - " + dto.getContact());
            }
        }
    }

    private void clearProductSelection() {
        selectedProduct = null;
        txtQty.clear();
        txtQty.setText("1");
        txtUnitPriceDisplay.clear();
        lblQtyOnHand.setText("0Kg");
        lblItemName.setText("-");
        lblItemCode.setText("-");
        cmbProduct.getEditor().clear();
        cmbProduct.getSelectionModel().clearSelection();
        repopulateAllProducts();
    }

    private void calculateTotal() {
        double netTotal = 0.0;
        for (CartTM tm : obList) {
            netTotal += tm.getTotal();
        }
        lblNetTotal.setText(String.format("Total: Rs. %.2f", netTotal));
    }

    @FXML
    void btnPlaceOrderOnAction(ActionEvent event) {
        resolveSelectedCustomer();
        if (selectedCustomer == null) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Please select a valid customer from the list first!");
            return;
        }

        if (obList.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING, "Cart is empty!");
            return;
        }

        try {
            String totalText = lblNetTotal.getText().replace("Total: Rs. ", "").trim();
            double netTotal = Double.parseDouble(totalText);

            boolean isDemoCustomer = AppConstants.DEMO_CUSTOMER_ID.equals(selectedCustomer.getCustId());
            String fxmlPath = isDemoCustomer ? "/view/BalanceDemoCust.fxml" : "/view/balance.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (isDemoCustomer) {
                BalanceDemoCustController demoController = loader.getController();
                demoController.setOrderData(obList, selectedCustomer, netTotal);
            } else {
                BalanceController balanceController = loader.getController();
                balanceController.setOrderData(obList, selectedCustomer, netTotal);
            }

            Stage balanceStage = new Stage();
            balanceStage.setScene(new Scene(root));
            balanceStage.setTitle("Payment / Balance");
            balanceStage.setResizable(false);
            balanceStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            balanceStage.showAndWait();

            boolean orderDone = false;
            if (isDemoCustomer) {
                orderDone = ((BalanceDemoCustController) loader.getController()).isOrderPlaced();
            } else {
                orderDone = ((BalanceController) loader.getController()).isOrderPlaced();
            }

            if (orderDone) {
                Stage stage = (Stage) tblCart.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }
}
