package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class OrdersData {
    private Long customerId;
    private LocalDateTime orderDate;
    private String orderStatus;
    private BigDecimal totalAmount;

    private List<OrderItemData> items = new ArrayList<>();
}
