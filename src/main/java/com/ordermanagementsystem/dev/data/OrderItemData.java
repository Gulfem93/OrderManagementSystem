package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemData {
    private Long orderId;
    private Long productId;

    private Long orderitemQuantity;
    private BigDecimal orderitemUnitPrice;
}
