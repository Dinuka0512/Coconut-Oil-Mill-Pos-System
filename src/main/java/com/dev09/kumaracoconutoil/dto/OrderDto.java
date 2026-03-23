package com.dev09.kumaracoconutoil.dto;

import jakarta.persistence.*;
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
}
