package com.dev09.kumaracoconutoil.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ComboBoxUtil {

    private static final String NAVIGATING_KEY = "combo.arrowNavigating";
    private static final String MASTER_KEY = "combo.masterItems";
    private static final String COMMITTED_VALUE_KEY = "combo.committedValue";
    private static final String COMMITTED_TEXT_KEY = "combo.committedText";
    private static final String FILTERING_KEY = "combo.filtering";
    private static final String SUSPEND_FILTER_KEY = "combo.suspendFilter";
    private static final String SEARCH_MANAGED_KEY = "combo.searchManaged";

    public static boolean isSearchManaged(ComboBox<?> comboBox) {
        return comboBox != null && Boolean.TRUE.equals(comboBox.getProperties().get(SEARCH_MANAGED_KEY));
    }

    private ComboBoxUtil() {
    }

    public static boolean isNavigationKey(KeyEvent event) {
        if (event == null || event.getCode() == null) return false;
        KeyCode code = event.getCode();
        return code == KeyCode.UP || code == KeyCode.DOWN
                || code == KeyCode.LEFT || code == KeyCode.RIGHT
                || code == KeyCode.ENTER || code == KeyCode.ESCAPE
                || code == KeyCode.TAB
                || code == KeyCode.PAGE_UP || code == KeyCode.PAGE_DOWN
                || code == KeyCode.HOME || code == KeyCode.END
                || code == KeyCode.SHIFT || code == KeyCode.CONTROL || code == KeyCode.ALT
                || code == KeyCode.CAPS || code == KeyCode.WINDOWS;
    }

    public static boolean isArrowNavigating(ComboBox<?> comboBox) {
        return comboBox != null && Boolean.TRUE.equals(comboBox.getProperties().get(NAVIGATING_KEY));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void enableArrowSelection(ComboBox<?> comboBoxRaw) {
        if (comboBoxRaw == null) return;
        if (Boolean.TRUE.equals(comboBoxRaw.getProperties().get("combo.arrowSelectionEnabled"))) return;
        comboBoxRaw.getProperties().put("combo.arrowSelectionEnabled", true);

        final ComboBox comboBox = comboBoxRaw;
        boolean editable = comboBox.isEditable();
        comboBox.getProperties().put(SEARCH_MANAGED_KEY, editable);
        captureMaster(comboBox);
        rememberCommitted(comboBox);
        installHighlightCellFactory(comboBox);

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            ObservableList<?> items = comboBox.getItems();
            int size = items == null ? 0 : items.size();

            if (code == KeyCode.ENTER) {
                if (!comboBox.isShowing()) {
                    return;
                }
                Object highlighted = getHighlightedItem(comboBox);
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                if (highlighted != null) {
                    commitSelection(comboBox, highlighted);
                } else {
                    restoreCommitted(comboBox);
                    comboBox.hide();
                }
                event.consume();
                return;
            }

            if (code == KeyCode.ESCAPE) {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
                try {
                    restoreCommitted(comboBox);
                    restoreAllItems(comboBox);
                } finally {
                    Platform.runLater(() -> comboBox.getProperties().put(SUSPEND_FILTER_KEY, false));
                }
                comboBox.hide();
                event.consume();
                return;
            }

            if (code == KeyCode.TAB) {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                return;
            }

            if (code != KeyCode.UP && code != KeyCode.DOWN) return;
            event.consume();
            if (size == 0) {
                // nothing to navigate in current (filtered) results
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                return;
            }

            if (!comboBox.isShowing()) {
                comboBox.show();
            }
            comboBox.getProperties().put(NAVIGATING_KEY, true);
            final int delta = code == KeyCode.DOWN ? 1 : -1;
            Platform.runLater(() -> {
                installPopupMouseHandler(comboBox);
                installHighlightCellFactory(comboBox);
                installPopupKeyHandler(comboBox);
                moveHighlight(comboBox, delta);
            });
        });

        comboBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                captureMasterIfNeeded(comboBox);
                if (comboBox.isEditable() && comboBox.getEditor() != null) {
                    String t = comboBox.getEditor().getText();
                    if (t == null || t.trim().isEmpty()) {
                        restoreAllItems(comboBox);
                    } else {
                        // Re-apply filter for current search text so results list is ready for arrow navigation
                        filterItems(comboBox, t);
                    }
                } else {
                    restoreAllItems(comboBox);
                }
                Platform.runLater(() -> {
                    installPopupMouseHandler(comboBox);
                    installHighlightCellFactory(comboBox);
                    installPopupKeyHandler(comboBox);
                    if (comboBox.isEditable() && comboBox.getEditor() != null) {
                        String t = comboBox.getEditor().getText();
                        if (t == null || t.trim().isEmpty()) {
                            restoreAllItems(comboBox);
                        }
                    }
                });
            } else {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
            }
        });

        comboBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (Boolean.TRUE.equals(comboBox.getProperties().get(FILTERING_KEY))) return;
            if (Boolean.TRUE.equals(comboBox.getProperties().get(SUSPEND_FILTER_KEY))) return;
            if (Boolean.TRUE.equals(comboBox.getProperties().get(NAVIGATING_KEY))) return;
            rememberCommitted(comboBox);
        });
        if (comboBox.getSelectionModel() != null) {
            comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                if (Boolean.TRUE.equals(comboBox.getProperties().get(FILTERING_KEY))) return;
                if (Boolean.TRUE.equals(comboBox.getProperties().get(SUSPEND_FILTER_KEY))) return;
                if (Boolean.TRUE.equals(comboBox.getProperties().get(NAVIGATING_KEY))) return;
                rememberCommitted(comboBox);
            });
        }

        // Managed search for editable combos: filter from master list on typing, no auto-commit.
        if (comboBox.isEditable() && comboBox.getEditor() != null) {
            comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
                if (Boolean.TRUE.equals(comboBox.getProperties().get(SUSPEND_FILTER_KEY))) return;
                if (Boolean.TRUE.equals(comboBox.getProperties().get(FILTERING_KEY))) return;
                if (Boolean.TRUE.equals(comboBox.getProperties().get(NAVIGATING_KEY))) return;
                if (!comboBox.getEditor().isFocused() && !comboBox.isFocused()) return;

                String text = newText == null ? "" : newText;
                String trimmed = text.trim();
                String committedText = Objects.toString(comboBox.getProperties().get(COMMITTED_TEXT_KEY), "");
                // If programmatically set to committed display and closed, avoid unnecessary refilter
                if (!comboBox.isShowing() && text.equals(committedText)) return;

                captureMasterIfNeeded(comboBox);
                filterItems(comboBox, trimmed);
            });
        }

        comboBox.itemsProperty().addListener((obs, oldItems, newItems) -> {
            if (Boolean.TRUE.equals(comboBox.getProperties().get(FILTERING_KEY))) return;
            captureMasterIfLikelyFull(comboBox);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void captureMaster(ComboBox comboBox) {
        ObservableList items = comboBox.getItems();
        if (items == null) return;
        if (items.isEmpty() && comboBox.getProperties().containsKey(MASTER_KEY)) {
            return;
        }
        comboBox.getProperties().put(MASTER_KEY, new ArrayList<>(items));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void captureMasterIfLikelyFull(ComboBox comboBox) {
        ObservableList items = comboBox.getItems();
        if (items == null || items.isEmpty()) return;
        Object masterObj = comboBox.getProperties().get(MASTER_KEY);
        if (!(masterObj instanceof List) || ((List<?>) masterObj).isEmpty()) {
            comboBox.getProperties().put(MASTER_KEY, new ArrayList<>(items));
            return;
        }
        List<?> master = (List<?>) masterObj;
        // Refresh master when list looks like a full/reset dataset (common after reload/repopulateAll)
        if (items.size() >= master.size()) {
            comboBox.getProperties().put(MASTER_KEY, new ArrayList<>(items));
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void captureMasterIfNeeded(ComboBox comboBox) {
        Object master = comboBox.getProperties().get(MASTER_KEY);
        if (!(master instanceof List) || ((List) master).isEmpty()) {
            captureMaster(comboBox);
        } else {
            captureMasterIfLikelyFull(comboBox);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<Object> getMaster(ComboBox comboBox) {
        Object master = comboBox.getProperties().get(MASTER_KEY);
        if (master instanceof List) {
            return (List<Object>) master;
        }
        ObservableList items = comboBox.getItems();
        return items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void filterItems(ComboBox comboBox, String query) {
        captureMasterIfNeeded(comboBox);
        List<Object> master = getMaster(comboBox);
        if (master.isEmpty() && comboBox.getItems() != null && !comboBox.getItems().isEmpty()) {
            captureMaster(comboBox);
            master = getMaster(comboBox);
        }
        ObservableList items = comboBox.getItems();
        if (items == null) return;

        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        comboBox.getProperties().put(FILTERING_KEY, true);
        try {
            if (q.isEmpty()) {
                if (!master.isEmpty() && (items.size() != master.size() || !items.equals(master))) {
                    items.setAll(master);
                }
            } else {
                List<Object> filtered = new ArrayList<>();
                for (Object item : master) {
                    String s = item == null ? "" : String.valueOf(item).toLowerCase(Locale.ROOT);
                    if (s.contains(q)) filtered.add(item);
                }
                items.setAll(filtered);
            }
        } finally {
            comboBox.getProperties().put(FILTERING_KEY, false);
        }

        // Show results list while searching/typing (no commit)
        if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getEditor().isFocused()) {
            if (!comboBox.isShowing()) comboBox.show();
        } else if (!q.isEmpty() && !comboBox.isShowing() && comboBox.isFocused()) {
            comboBox.show();
        }

        // Keep popup highlight independent; clear stale highlight when results change
        Platform.runLater(() -> {
            try {
                if (!comboBox.isShowing()) return;
                ListView lv = getPopupListView(comboBox);
                if (lv == null || lv.getSelectionModel() == null) return;
                ObservableList lvItems = lv.getItems();
                if (lvItems == null || lvItems.isEmpty()) {
                    lv.getSelectionModel().clearSelection();
                    return;
                }
                int idx = lv.getSelectionModel().getSelectedIndex();
                if (idx >= lvItems.size()) {
                    lv.getSelectionModel().clearSelection();
                }
                // Do not auto-select first result on typing (avoid "typed letter selects result"); arrows will highlight.
            } catch (Exception ignored) {
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void restoreAllItems(ComboBox comboBox) {
        List<Object> master = getMaster(comboBox);
        ObservableList items = comboBox.getItems();
        if (items == null) return;
        if (master.isEmpty()) return;
        comboBox.getProperties().put(FILTERING_KEY, true);
        try {
            if (items.size() != master.size() || !items.equals(master)) {
                items.setAll(master);
            }
        } finally {
            comboBox.getProperties().put(FILTERING_KEY, false);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void restoreAllItemsIfEditorEmpty(ComboBox comboBox) {
        if (comboBox.isEditable() && comboBox.getEditor() != null) {
            String t = comboBox.getEditor().getText();
            if (t == null || t.trim().isEmpty()) {
                restoreAllItems(comboBox);
            }
        } else {
            // non-editable: ensure full list
            restoreAllItems(comboBox);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void rememberCommitted(ComboBox comboBox) {
        Object value = comboBox.getValue();
        if (value == null && comboBox.getSelectionModel() != null) {
            value = comboBox.getSelectionModel().getSelectedItem();
        }
        comboBox.getProperties().put(COMMITTED_VALUE_KEY, value);
        if (comboBox.isEditable() && comboBox.getEditor() != null) {
            String txt = comboBox.getEditor().getText();
            // if value is set, prefer its string form when editor matches or is empty? keep actual editor text
            if ((txt == null || txt.isEmpty()) && value != null) txt = String.valueOf(value);
            comboBox.getProperties().put(COMMITTED_TEXT_KEY, txt == null ? "" : txt);
        } else {
            comboBox.getProperties().put(COMMITTED_TEXT_KEY, value == null ? "" : String.valueOf(value));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void restoreCommitted(ComboBox comboBox) {
        Object committed = comboBox.getProperties().get(COMMITTED_VALUE_KEY);
        String committedText = Objects.toString(comboBox.getProperties().get(COMMITTED_TEXT_KEY), "");
        comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
        try {
            if (comboBox.getSelectionModel() != null) {
                if (committed != null) {
                    comboBox.getSelectionModel().select(committed);
                } else {
                    comboBox.getSelectionModel().clearSelection();
                }
            }
            comboBox.setValue(committed);
            if (comboBox.isEditable() && comboBox.getEditor() != null) {
                comboBox.getEditor().setText(committed != null ? (committedText.isEmpty() ? String.valueOf(committed) : committedText) : "");
                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
            }
        } finally {
            comboBox.getProperties().put(SUSPEND_FILTER_KEY, false);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void moveHighlight(ComboBox comboBox, int delta) {
        moveHighlight(comboBox, delta, 0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void moveHighlight(ComboBox comboBox, int delta, int attempt) {
        try {
            if (comboBox == null || !comboBox.isShowing()) return;
            comboBox.getProperties().put(NAVIGATING_KEY, true);
            ObservableList items = comboBox.getItems();
            if (items == null || items.isEmpty()) return;

            ListView listView = getPopupListView(comboBox);
            if (listView == null || listView.getSelectionModel() == null) {
                if (attempt < 5) {
                    final int next = attempt + 1;
                    Platform.runLater(() -> moveHighlight(comboBox, delta, next));
                }
                return;
            }

            ObservableList lvItems = listView.getItems();
            ObservableList activeItems = (lvItems != null && !lvItems.isEmpty()) ? lvItems : items;
            int size = activeItems.size();
            if (size <= 0) {
                if (attempt < 5) {
                    final int next = attempt + 1;
                    Platform.runLater(() -> moveHighlight(comboBox, delta, next));
                }
                return;
            }

            int current = listView.getSelectionModel().getSelectedIndex();
            if (current >= size) current = -1;

            int target;
            if (delta > 0) {
                target = current < 0 ? 0 : Math.min(current + 1, size - 1);
            } else {
                target = current < 0 ? size - 1 : Math.max(current - 1, 0);
            }
            if (target < 0 || target >= size) return;
            final int targetIndex = target;

            // During navigation, if skin sync updates combo value/editor, keep committed state separate and preserve editor text for search.
            String typedQuery = null;
            if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getEditor().isFocused()) {
                typedQuery = comboBox.getEditor().getText();
            }
            final String queryToPreserve = typedQuery;

            listView.getSelectionModel().clearAndSelect(targetIndex);
            if (listView.getFocusModel() != null) {
                listView.getFocusModel().focus(targetIndex);
            }
            listView.scrollTo(targetIndex);

            if (queryToPreserve != null) {
                Platform.runLater(() -> {
                    try {
                        if (!comboBox.isShowing()) return;
                        if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getEditor().isFocused()) {
                            comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
                            try {
                                if (!Objects.equals(comboBox.getEditor().getText(), queryToPreserve)) {
                                    comboBox.getEditor().setText(queryToPreserve);
                                    comboBox.getEditor().positionCaret(queryToPreserve.length());
                                }
                            } finally {
                                comboBox.getProperties().put(SUSPEND_FILTER_KEY, false);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                });
            }

            Platform.runLater(() -> {
                try {
                    if (!comboBox.isShowing()) return;
                    ListView lv = getPopupListView(comboBox);
                    if (lv == null || lv.getSelectionModel() == null) return;
                    ObservableList now = lv.getItems();
                    if (now == null || targetIndex >= now.size()) return;
                    lv.getSelectionModel().clearAndSelect(targetIndex);
                    if (lv.getFocusModel() != null) lv.getFocusModel().focus(targetIndex);
                    lv.scrollTo(targetIndex);
                    if (queryToPreserve != null && comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getEditor().isFocused()) {
                        comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
                        try {
                            if (!Objects.equals(comboBox.getEditor().getText(), queryToPreserve)) {
                                comboBox.getEditor().setText(queryToPreserve);
                                comboBox.getEditor().positionCaret(queryToPreserve.length());
                            }
                        } finally {
                            comboBox.getProperties().put(SUSPEND_FILTER_KEY, false);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static Object getHighlightedItem(ComboBox comboBox) {
        ListView listView = getPopupListView(comboBox);
        if (listView != null && listView.getSelectionModel() != null) {
            Object selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) return selected;
            int idx = listView.getSelectionModel().getSelectedIndex();
            ObservableList items = comboBox.getItems();
            if (idx >= 0 && items != null && idx < items.size()) return items.get(idx);
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void commitSelection(ComboBox comboBox, Object item) {
        if (item == null) {
            comboBox.hide();
            comboBox.getProperties().put(NAVIGATING_KEY, false);
            return;
        }
        comboBox.getProperties().put(NAVIGATING_KEY, false);
        comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
        try {
            restoreAllItems(comboBox);
            if (comboBox.getSelectionModel() != null) {
                comboBox.getSelectionModel().select(item);
            }
            comboBox.setValue(item);
            if (comboBox.isEditable() && comboBox.getEditor() != null) {
                String text = String.valueOf(item);
                comboBox.getEditor().setText(text);
                comboBox.getEditor().positionCaret(text.length());
            }
            rememberCommitted(comboBox);
        } finally {
            Platform.runLater(() -> comboBox.getProperties().put(SUSPEND_FILTER_KEY, false));
        }
        comboBox.hide();
        // Fire action once for committed selection (FXML onAction handlers rely on this)
        comboBox.fireEvent(new javafx.event.ActionEvent(comboBox, comboBox));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void installPopupMouseHandler(ComboBox comboBox) {
        try {
            ListView listView = getPopupListView(comboBox);
            if (listView == null) return;
            if (Boolean.TRUE.equals(listView.getProperties().get("combo.mouseCommitHandlerInstalled"))) return;
            listView.getProperties().put("combo.mouseCommitHandlerInstalled", true);
            listView.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                Platform.runLater(() -> {
                    if (!comboBox.isShowing() && comboBox.getScene() == null) return;
                    comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
                    try {
                        Object v = comboBox.getValue();
                        if (v == null && comboBox.getSelectionModel() != null) v = comboBox.getSelectionModel().getSelectedItem();
                        restoreAllItems(comboBox);
                        if (v != null) {
                            if (comboBox.getSelectionModel() != null) comboBox.getSelectionModel().select(v);
                            comboBox.setValue(v);
                            if (comboBox.isEditable() && comboBox.getEditor() != null) {
                                comboBox.getEditor().setText(String.valueOf(v));
                                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                            }
                        }
                        rememberCommitted(comboBox);
                    } finally {
                        comboBox.getProperties().put(SUSPEND_FILTER_KEY, false);
                    }
                });
            });
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void installPopupKeyHandler(ComboBox comboBox) {
        try {
            ListView listView = getPopupListView(comboBox);
            if (listView == null) return;
            if (Boolean.TRUE.equals(listView.getProperties().get("combo.popupKeyHandlerInstalled"))) return;
            listView.getProperties().put("combo.popupKeyHandlerInstalled", true);
            listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                    event.consume();
                    // Always perform our own highlight movement to avoid JavaFX default behavior conflicts.
                    int delta = event.getCode() == KeyCode.DOWN ? 1 : -1;
                    comboBox.getProperties().put(NAVIGATING_KEY, true);
                    moveHighlight(comboBox, delta);
                } else if (event.getCode() == KeyCode.ENTER) {
                    // Let combo-level handler logic work consistently; consume here and trigger commit path
                    event.consume();
                    if (!comboBox.isShowing()) return;
                    Object highlighted = getHighlightedItem(comboBox);
                    comboBox.getProperties().put(NAVIGATING_KEY, false);
                    if (highlighted != null) {
                        commitSelection(comboBox, highlighted);
                    }
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    event.consume();
                    comboBox.getProperties().put(NAVIGATING_KEY, false);
                    comboBox.getProperties().put(SUSPEND_FILTER_KEY, true);
                    try {
                        restoreCommitted(comboBox);
                        restoreAllItems(comboBox);
                    } finally {
                        Platform.runLater(() -> comboBox.getProperties().put(SUSPEND_FILTER_KEY, false));
                    }
                    comboBox.hide();
                }
            });
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void installHighlightCellFactory(ComboBox comboBox) {
        try {
            Callback existing = comboBox.getCellFactory();
            Object marker = comboBox.getProperties().get("combo.highlightCellFactoryInstalled");
            if (Boolean.TRUE.equals(marker)) return;

            comboBox.setCellFactory(lv -> {
                ListCell cell;
                if (existing != null) {
                    try {
                        Object c = existing.call(lv);
                        cell = (c instanceof ListCell) ? (ListCell) c : new ListCell();
                    } catch (Exception ex) {
                        cell = new ListCell();
                    }
                } else {
                    cell = new ListCell() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : String.valueOf(item));
                        }
                    };
                }
                // Enhance update to show clear highlight for focused/selected (navigated) item in popup
                final ListCell base = cell;
                // We can't easily override updateItem of arbitrary cell; wrap by listening to selection/focus state
                base.selectedProperty().addListener((o, a, selected) -> applyHighlightStyle(base));
                base.hoverProperty().addListener((o, a, hover) -> applyHighlightStyle(base));
                base.focusedProperty().addListener((o, a, f) -> applyHighlightStyle(base));
                base.itemProperty().addListener((o, a, b) -> applyHighlightStyle(base));
                base.emptyProperty().addListener((o, a, e) -> applyHighlightStyle(base));
                Platform.runLater(() -> applyHighlightStyle(base));
                return base;
            });
            comboBox.getProperties().put("combo.highlightCellFactoryInstalled", true);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static void applyHighlightStyle(ListCell cell) {
        try {
            if (cell == null) return;
            String base = cell.getStyle() == null ? "" : cell.getStyle();
            String cleaned = base
                    .replace("-fx-background-color: #2563eb;", "")
                    .replace("-fx-background-color: #2563eb", "")
                    .replace("-fx-text-fill: white;", "")
                    .replace("-fx-text-fill: white", "")
                    .replace("-fx-background-color: #dbeafe;", "")
                    .replace("-fx-background-color: #dbeafe", "")
                    .replace("-fx-text-fill: #0f172a;", "")
                    .replace("-fx-text-fill: #0f172a", "");
            if (cell.isEmpty() || cell.getItem() == null) {
                if (!cleaned.equals(base)) cell.setStyle(cleaned);
                return;
            }
            if (cell.isSelected() || cell.isFocused()) {
                // Blue highlight for the currently navigated/highlighted option in results
                String sep = (cleaned.endsWith(";") || cleaned.isEmpty()) ? "" : ";";
                cell.setStyle(cleaned + sep + "-fx-background-color: #2563eb; -fx-text-fill: white;");
            } else {
                if (!cleaned.equals(base)) cell.setStyle(cleaned);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings({"rawtypes"})
    private static ListView getPopupListView(ComboBox comboBox) {
        try {
            if (comboBox.getSkin() instanceof ComboBoxListViewSkin) {
                Node popupContent = ((ComboBoxListViewSkin) comboBox.getSkin()).getPopupContent();
                if (popupContent instanceof ListView) {
                    return (ListView) popupContent;
                }
                ListView found = findListView(popupContent);
                if (found != null) return found;
            }
        } catch (Exception ignored) {
        }
        try {
            Node node = comboBox.lookup(".list-view");
            if (node instanceof ListView) {
                return (ListView) node;
            }
            if (node != null) {
                ListView found = findListView(node);
                if (found != null) return found;
            }
        } catch (Exception ignored) {
        }
        try {
            if (comboBox.getSkin() != null) {
                Node n = comboBox.getSkin().getNode();
                if (n != null) {
                    Node lv = n.lookup(".list-view");
                    if (lv instanceof ListView) return (ListView) lv;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings({"rawtypes"})
    private static ListView findListView(Node node) {
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
}
