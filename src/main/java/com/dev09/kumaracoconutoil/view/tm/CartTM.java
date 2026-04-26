package com.dev09.kumaracoconutoil.view.tm;

import javafx.scene.control.Button;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CartTM {
    private String code;
    private String description;
    private double qty;
    private double unitPrice;
    private double total;
    private Button btn;
}
