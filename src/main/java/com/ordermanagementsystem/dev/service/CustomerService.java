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
import com.ordermanagementsystem.dev.data.CustomersData;
import com.ordermanagementsystem.dev.data.OrderView;
import com.ordermanagementsystem.dev.model.CustomerModel;
import com.ordermanagementsystem.dev.repository.CustomerRepository;

@Service
public class CustomerService {
    final private CustomerRepository customerRepository;
    final private OrderService orderService;

    public CustomerService(CustomerRepository customerRepository, OrderService orderService) {
        this.customerRepository = customerRepository;
        this.orderService = orderService;
    }

    public CustomerModel customers(CustomersData customersData) {
        if (customerRepository.existsByCustomerEmail(customersData.getCustomerEmail())) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }
        CustomerModel cModel = new CustomerModel();
        cModel.setCustomerEmail(customersData.getCustomerEmail());
        cModel.setCustomerName(customersData.getCustomerName());
        cModel.setCustomerSurname(customersData.getCustomerSurname());
        cModel.setCustomerPhone(customersData.getCustomerPhone());

        return customerRepository.save(cModel);
    }

    public long countCustomers() {
        return customerRepository.count();
    }

    // Owner: şirketinden alışveriş yapan müşteriler, en çok harcayan üstte.
    // Harcamaya iptal ve iade edilen siparişler dahil edilmez.
    public List<CustomerSummary> getCustomersForCompany(String company) {
        Map<Long, List<OrderView>> ordersByCustomer = new LinkedHashMap<>();
        for (OrderView view : orderService.getOrderViewsForCompany(company, null)) {
            ordersByCustomer
                    .computeIfAbsent(view.order().getCustomer().getCustomerId(), id -> new ArrayList<>())
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
