package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ordermanagementsystem.dev.model.CustomerModel;

// Owner'ın Customers sayfasındaki bir satır: müşteri ve owner'ın şirketinden yaptığı alışverişler
public record CustomerSummary(CustomerModel customer, long orderCount, BigDecimal totalSpent,
        LocalDateTime lastOrderDate) {
}
