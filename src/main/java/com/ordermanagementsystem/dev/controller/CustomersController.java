package com.ordermanagementsystem.dev.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.CustomerSummary;
import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.CustomerService;
import com.ordermanagementsystem.dev.service.UserService;

// /customers/** sadece ROLE_OWNER (SecurityConfig)
@Controller
public class CustomersController {

    private final CustomerService customerService;
    private final UserService userService;

    public CustomersController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    // Owner: şirketinden alışveriş yapan müşteriler
    @GetMapping("/customers")
    public String getCustomersPage(Authentication auth, Model model) {
        UsersData userData = userService.getUserData(auth.getName());
        List<CustomerSummary> customers = customerService.getCustomersForCompany(userData.getCompanyName());

        model.addAttribute("userData", userData);
        model.addAttribute("isOwner", true);
        model.addAttribute("customers", customers);
        model.addAttribute("totalRevenue", customers.stream()
                .map(CustomerSummary::totalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "customers";
    }
}
