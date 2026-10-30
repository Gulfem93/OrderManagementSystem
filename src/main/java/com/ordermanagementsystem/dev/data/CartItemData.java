package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class CartItemData {
    private Long productId;
    private Integer quantity;
    private Long orderId;
    private Long cartNo;
    private String cartUserneme;
}
