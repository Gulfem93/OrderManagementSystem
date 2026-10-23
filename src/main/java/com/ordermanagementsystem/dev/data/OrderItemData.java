package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class OrderItemData {
    private Long orderitem_custumerId;
    private Long orderitem_productId;

    private Long orderitemQuantity;
    private Long orderitemUnitPrice;
}
