package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.util.List;

import com.ordermanagementsystem.dev.model.OrderItemModel;
import com.ordermanagementsystem.dev.model.OrdersModel;

// Orders sayfasında gösterilen sipariş.
// Customer siparişin tüm kalemlerini, owner sadece kendi şirketinin kalemlerini görür;
// total da buna göre hesaplanır.
public record OrderView(OrdersModel order, List<OrderItemModel> items, BigDecimal total) {
}
