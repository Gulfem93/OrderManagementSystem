package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ordermanagementsystem.dev.model.UsersModel;

// Owner'ın Customers sayfasındaki bir satır: müşteri ve owner'ın şirketinden yaptığı alışverişler
public record CustomerSummary(UsersModel customer, long orderCount, BigDecimal totalSpent,
        LocalDateTime lastOrderDate) {
}
