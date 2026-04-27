package com.dev09.kumaracoconutoil.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderDto {
    private String orderId;
    private LocalDate date;
    private CustomerDto customer;
    private List<OrderProductDetailsDto> orderProducts;
    private double amountPaid;
    private double discount;
    private double tax;
    private double balance;

    public String getCustomerName() {
        return customer != null ? customer.getName() : "";
    }

    public String getContact() {
        return customer != null ? customer.getContact() : "";
    }

    public double getTotal() {
        return amountPaid;
    }
}
