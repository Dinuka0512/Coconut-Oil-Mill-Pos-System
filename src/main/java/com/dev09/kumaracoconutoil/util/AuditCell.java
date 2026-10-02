package com.dev09.kumaracoconutoil.util;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public final class AuditCell {

    private AuditCell() {
    }

    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> factory() {
        return param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                boolean blank = empty || item == null || item.isEmpty();
                setText(blank ? null : item);
                setTooltip(blank ? null : new javafx.scene.control.Tooltip(item));
                setWrapText(false);
                setAlignment(Pos.CENTER);
                setStyle("-fx-text-alignment: center;");
            }
        };
    }
}