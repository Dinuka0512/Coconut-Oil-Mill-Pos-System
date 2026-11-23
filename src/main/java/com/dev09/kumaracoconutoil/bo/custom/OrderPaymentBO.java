package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.OrderPaymentDto;

import java.util.ArrayList;

public interface OrderPaymentBO extends SuperBo {
    boolean savePayment(OrderPaymentDto dto);
    ArrayList<OrderPaymentDto> getPaymentsByOrderId(String orderId);
}
