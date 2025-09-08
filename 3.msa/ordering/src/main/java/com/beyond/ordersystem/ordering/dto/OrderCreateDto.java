package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCreateDto {
    private Long productId;
    private int productCount;

    public OrderDetail toEntity(Ordering ordering) {
        return OrderDetail.builder()
                .productId(this.productId)
                .quantity(this.productCount)
                .ordering(ordering)
                .build();
    }
}
