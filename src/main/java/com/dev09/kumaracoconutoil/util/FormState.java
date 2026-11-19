package com.dev09.kumaracoconutoil.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captures the values a user has typed into a screen and puts them back later.
 *
 * <p>The snapshot only holds strings in a small map - no JavaFX nodes, controllers or
 * scene graph references are kept - so capturing at logout is a single cheap walk of
 * the view and the data safely survives closing the windows.</p>
 *
 * <p>Password fields are deliberately never captured.</p>
 */
public final class FormState {

    private FormState() {
    }

    /** One snapshot entry per editable control, keyed by a stable path in the view. */
    public static Map<String, String> capture(Parent root) {
        Map<String, String> values = new LinkedHashMap<>();
        walk(root, new HashMap<>(), (node, key) -> {
            try {
                if (node instanceof TextInputControl text && !(node instanceof PasswordField)) {
                    put(values, key, "text", text.getText());
                } else if (node instanceof ComboBox<?> combo) {
                    Object value = combo.getValue();
                    put(values, key, "value", value == null ? "" : String.valueOf(value));
                    if (combo.getEditor() != null) {
                        put(values, key, "editor", combo.getEditor().getText());
                    }
                } else if (node instanceof ChoiceBox<?> choice) {
                    Object value = choice.getValue();
                    put(values, key, "value", value == null ? "" : String.valueOf(value));
                } else if (node instanceof DatePicker date) {
                    put(values, key, "value", date.getValue() == null ? "" : date.getValue().toString());
                } else if (node instanceof ToggleButton toggle) { // also covers CheckBox / RadioButton
                    put(values, key, "selected", String.valueOf(toggle.isSelected()));
                } else if (node instanceof Slider slider) {
                    put(values, key, "value", String.valueOf(slider.getValue()));
                } else if (node instanceof Spinner<?> spinner) {
                    Object value = spinner.getValue();
                    put(values, key, "value", value == null ? "" : String.valueOf(value));
                } else if (node instanceof TableView<?> table) {
                    put(values, key, "row", String.valueOf(table.getSelectionModel().getSelectedIndex()));
                }
            } catch (Exception ignored) {
                // A single odd control must never break logout.
            }
        });
        return values;
    }

    /** Restores previously captured values onto an equivalent, freshly loaded view. */
    public static void restore(Node root, Map<String, String> values) {
        if (values == null || values.isEmpty()) return;
        walk(root, new HashMap<>(), (node, key) -> {
            try {
                if (node instanceof PasswordField) {
                    return;
                }
                if (node instanceof TextInputControl text) {
                    String v = values.get(key + ".text");
                    if (v != null) text.setText(v);
                } else if (node instanceof ComboBox<?> combo) {
                    String v = values.get(key + ".value");
                    if (v != null && !v.isEmpty()) setRawValue(combo, v);
                    String editor = values.get(key + ".editor");
                    if (editor != null && combo.getEditor() != null) combo.getEditor().setText(editor);
                } else if (node instanceof ChoiceBox<?> choice) {
                    String v = values.get(key + ".value");
                    if (v != null && !v.isEmpty()) setRawValue(choice, v);
                } else if (node instanceof DatePicker date) {
                    String v = values.get(key + ".value");
                    if (v != null && !v.isEmpty()) date.setValue(java.time.LocalDate.parse(v));
                } else if (node instanceof ToggleButton toggle) {
                    String v = values.get(key + ".selected");
                    if (v != null) toggle.setSelected(Boolean.parseBoolean(v));
                } else if (node instanceof Slider slider) {
                    String v = values.get(key + ".value");
                    if (v != null) slider.setValue(Double.parseDouble(v));
                } else if (node instanceof Spinner<?> spinner) {
                    String v = values.get(key + ".value");
                    if (v != null && !v.isEmpty()) setRawValue(spinner, v);
                } else if (node instanceof TableView<?> table) {
                    String v = values.get(key + ".row");
                    if (v != null) {
                        int row = Integer.parseInt(v);
                        if (row >= 0 && row < table.getItems().size()) table.getSelectionModel().select(row);
                    }
                }
            } catch (Exception ignored) {
                // A stale value must never prevent the rest of the form from loading.
            }
        });
    }

    private static void put(Map<String, String> values, String key, String property, String value) {
        values.put(key + "." + property, value == null ? "" : value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setRawValue(Object control, String value) {
        if (control instanceof ComboBox<?> combo) ((ComboBox) combo).setValue(value);
        else if (control instanceof ChoiceBox<?> choice) ((ChoiceBox) choice).setValue(value);
        else if (control instanceof Spinner<?> spinner && spinner.getValueFactory() != null) {
            ((SpinnerValueFactory) spinner.getValueFactory()).setValue(value);
        }
    }

    /**
     * Depth-first walk. An fx:id is used when present; otherwise a deterministic
     * "Class#n" key is generated, so a freshly loaded FXML gets identical keys.
     */
    private static void walk(Node root, Map<String, Integer> counters, Visitor visitor) {
        if (root == null) return;
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            String id = node.getId();
            String key;
            if (id != null && !id.isBlank()) {
                key = "id:" + id;
            } else {
                String simple = node.getClass().getSimpleName();
                int n = counters.merge(simple, 1, Integer::sum);
                key = simple + "#" + n;
            }
            visitor.visit(node, key);
            if (node instanceof Parent parent) {
                for (Node child : parent.getChildrenUnmodifiable()) stack.push(child);
            }
        }
    }

    @FunctionalInterface
    private interface Visitor {
        void visit(Node node, String key);
    }
}
