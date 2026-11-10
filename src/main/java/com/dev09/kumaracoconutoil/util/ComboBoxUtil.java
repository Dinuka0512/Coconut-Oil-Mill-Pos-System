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
            if (code != KeyCode.UP && code != KeyCode.DOWN) return;
            // Let editable combo's popup/editor handle arrows in their natural way when showing
            if (comboBox.isEditable() && comboBox.isShowing()) return;

            int size = comboBox.getItems() == null ? 0 : comboBox.getItems().size();
            if (size == 0) return;

            int index = comboBox.getSelectionModel().getSelectedIndex();
            if (code == KeyCode.DOWN) {
                int next = index < 0 ? 0 : Math.min(index + 1, size - 1);
                comboBox.getSelectionModel().select(next);
                comboBox.getEditor();
                if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getValue() != null) {
                    comboBox.getEditor().setText(String.valueOf(comboBox.getValue()));
                    comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                }
                event.consume();
            } else if (code == KeyCode.UP) {
                int prev = index <= 0 ? 0 : index - 1;
                if (index < 0) prev = 0;
                comboBox.getSelectionModel().select(prev);
                if (comboBox.isEditable() && comboBox.getEditor() != null && comboBox.getValue() != null) {
                    comboBox.getEditor().setText(String.valueOf(comboBox.getValue()));
                    comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                }
                event.consume();
            }
        });
    }
}
