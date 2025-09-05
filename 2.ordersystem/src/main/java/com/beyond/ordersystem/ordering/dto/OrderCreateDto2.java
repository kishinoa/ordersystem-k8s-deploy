package com.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCreateDto2 {
    private List<OrderCreateDto> details;
    private Long storeId;
    private String payment;
}
