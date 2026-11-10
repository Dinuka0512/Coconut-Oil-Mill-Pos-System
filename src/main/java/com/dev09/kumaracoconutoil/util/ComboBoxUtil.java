package com.dev09.kumaracoconutoil.util;

import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class ComboBoxUtil {

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

    public static void enableArrowSelection(ComboBox<?> comboBox) {
        if (comboBox == null) return;

        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.ENTER) {
                // If popup is open, let the popup/list handle Enter to confirm the highlighted value
                if (comboBox.isShowing()) {
                    return;
                }
                // If closed, keep default behavior (form submit handlers etc.)
                return;
            }

            if (code != KeyCode.UP && code != KeyCode.DOWN) return;

            int size = comboBox.getItems() == null ? 0 : comboBox.getItems().size();
            if (size == 0) return;

            // When popup is already showing, let the dropdown list handle arrow navigation/selection
            if (comboBox.isShowing()) {
                return;
            }

            // Show the list so the user can see all/available options when navigating by arrow keys
            comboBox.show();

            int index = comboBox.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                // try to infer from current editor/value if possible
                Object value = comboBox.getValue();
                if (value == null && comboBox.isEditable() && comboBox.getEditor() != null) {
                    value = comboBox.getEditor().getText();
                }
                if (value != null) {
                    String v = String.valueOf(value);
                    for (int i = 0; i < size; i++) {
                        Object item = comboBox.getItems().get(i);
                        if (item != null && String.valueOf(item).equalsIgnoreCase(v)) {
                            index = i;
                            break;
                        }
                    }
                }
            }

            int target;
            if (code == KeyCode.DOWN) {
                target = index < 0 ? 0 : Math.min(index + 1, size - 1);
            } else {
                target = index <= 0 ? 0 : index - 1;
                if (index < 0) target = 0;
            }

            comboBox.getSelectionModel().select(target);
            if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getValue() != null) {
                comboBox.getEditor().setText(String.valueOf(comboBox.getValue()));
                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
            }

            // Keep focus behavior consistent; let popup remain open for visibility/confirmation with Enter
            event.consume();
        });
    }
}
