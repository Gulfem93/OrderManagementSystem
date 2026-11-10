package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.data.CustomerSummary;
import com.ordermanagementsystem.dev.data.OrderView;

// Owner > Customers: müşteri = owner'ın ürününü sipariş etmiş kullanıcı (users tablosu)
@Service
public class CustomerService {
    private final OrderService orderService;

    public CustomerService(OrderService orderService) {
        this.orderService = orderService;
    }

    // Owner: şirketinden alışveriş yapan müşteriler, en çok harcayan üstte.
    // Harcamaya iptal ve iade edilen siparişler dahil edilmez.
    public List<CustomerSummary> getCustomersForCompany(Long companyId) {
        Map<Long, List<OrderView>> ordersByCustomer = new LinkedHashMap<>();
        for (OrderView view : orderService.getOrderViewsForCompany(companyId, null)) {
            ordersByCustomer
                    .computeIfAbsent(view.order().getCustomer().getUserId(), id -> new ArrayList<>())
                    .add(view);
        }

        return ordersByCustomer.values().stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(CustomerSummary::totalSpent).reversed())
                .toList();
    }

    private CustomerSummary toSummary(List<OrderView> views) {
        BigDecimal totalSpent = views.stream()
                .filter(view -> view.order().getOrderStatus() == null
                        || !view.order().getOrderStatus().restoresStock())
                .map(OrderView::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime lastOrderDate = views.stream()
                .map(view -> view.order().getOrderDate())
                .filter(date -> date != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
        return new CustomerSummary(views.get(0).order().getCustomer(), views.size(), totalSpent, lastOrderDate);
    }
}
