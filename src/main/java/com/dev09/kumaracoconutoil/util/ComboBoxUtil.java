package com.dev09.kumaracoconutoil.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class ComboBoxUtil {

    private static final String NAVIGATING_KEY = "combo.arrowNavigating";

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

    public static void enableArrowSelection(ComboBox<?> comboBox) {
        if (comboBox == null) return;
        if (Boolean.TRUE.equals(comboBox.getProperties().get("combo.arrowSelectionEnabled"))) return;
        comboBox.getProperties().put("combo.arrowSelectionEnabled", true);

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            ObservableList<?> items = comboBox.getItems();
            int size = items == null ? 0 : items.size();

            if (code == KeyCode.ENTER) {
                if (!comboBox.isShowing()) {
                    return;
                }
                // Commit only the highlighted popup item on Enter.
                Object highlighted = getHighlightedItem(comboBox);
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                if (highlighted != null) {
                    commitSelection(comboBox, highlighted);
                } else {
                    comboBox.hide();
                }
                event.consume();
                return;
            }

            if (code == KeyCode.ESCAPE) {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                return; // let default close behavior run
            }

            if (code == KeyCode.TAB) {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
                return;
            }

            if (code != KeyCode.UP && code != KeyCode.DOWN) return;
            if (size == 0) return;

            if (!comboBox.isShowing()) {
                comboBox.show();
            }
            comboBox.getProperties().put(NAVIGATING_KEY, true);
            installPopupMouseHandler(comboBox);
            moveHighlight(comboBox, code == KeyCode.DOWN ? 1 : -1);
            event.consume();
        });

        comboBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                // Ensure mouse click commits work even after arrow navigation.
                Platform.runLater(() -> installPopupMouseHandler(comboBox));
            } else {
                comboBox.getProperties().put(NAVIGATING_KEY, false);
            }
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void moveHighlight(ComboBox comboBox, int delta) {
        Platform.runLater(() -> {
            ObservableList items = comboBox.getItems();
            if (items == null || items.isEmpty()) return;

            ListView listView = getPopupListView(comboBox);
            if (listView == null || listView.getSelectionModel() == null) {
                // Do not change committed combo value/selection when popup list view isn't available.
                return;
            }

            int current = listView.getSelectionModel().getSelectedIndex();

            if (current < 0) {
                Object currentValue = comboBox.getValue();
                if ((currentValue == null || String.valueOf(currentValue).trim().isEmpty())
                        && comboBox.isEditable() && comboBox.getEditor() != null) {
                    currentValue = comboBox.getEditor().getText();
                }
                if (currentValue != null) {
                    String v = String.valueOf(currentValue).trim().toLowerCase();
                    if (!v.isEmpty()) {
                        for (int i = 0; i < items.size(); i++) {
                            Object item = items.get(i);
                            if (item == null) continue;
                            String s = String.valueOf(item).toLowerCase();
                            if (s.equals(v) || s.startsWith(v) || s.contains(v)) {
                                current = i;
                                break;
                            }
                        }
                    }
                }
            }

            int target;
            if (delta > 0) {
                target = current < 0 ? 0 : Math.min(current + 1, items.size() - 1);
            } else {
                target = current <= 0 ? 0 : current - 1;
                if (current < 0) target = Math.max(items.size() - 1, 0);
            }
            final int targetIndex = target;

            // Highlight only in popup list view; do NOT change committed combo value/editor.
            listView.getSelectionModel().clearAndSelect(targetIndex);
            if (listView.getFocusModel() != null) {
                listView.getFocusModel().focus(targetIndex);
            }
            listView.scrollTo(targetIndex);

            Platform.runLater(() -> {
                try {
                    if (listView.getSelectionModel() != null) {
                        listView.getSelectionModel().clearAndSelect(targetIndex);
                    }
                    if (listView.getFocusModel() != null) {
                        listView.getFocusModel().focus(targetIndex);
                    }
                    listView.scrollTo(targetIndex);
                } catch (Exception ignored) {
                }
            });
        });
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
        if (comboBox.getSelectionModel() != null) {
            comboBox.getSelectionModel().select(item);
        }
        comboBox.setValue(item);
        if (comboBox.isEditable() && comboBox.getEditor() != null) {
            comboBox.getEditor().setText(String.valueOf(item));
            comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
        }
        comboBox.hide();
        // FXML onAction handlers are wired to the ComboBox; fire once to ensure confirmed commit is processed.
        comboBox.fireEvent(new javafx.event.ActionEvent(comboBox, comboBox));
    }

    @SuppressWarnings({"rawtypes"})
    private static void installPopupMouseHandler(ComboBox comboBox) {
        try {
            ListView listView = getPopupListView(comboBox);
            if (listView == null) return;
            if (Boolean.TRUE.equals(listView.getProperties().get("combo.mouseCommitHandlerInstalled"))) return;
            listView.getProperties().put("combo.mouseCommitHandlerInstalled", true);
            listView.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                // Mouse click should commit selection (default behavior). Clear nav flag so handlers run.
                comboBox.getProperties().put(NAVIGATING_KEY, false);
            });
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

    @SuppressWarnings({"rawtypes"})
    private static void installHighlightCellFactory(ComboBox comboBox) {
        // Intentionally not overriding cell factory to avoid changing existing UI design/CSS.
        // Highlight visibility is handled by popup list selection/focus during arrow navigation.
    }
}
