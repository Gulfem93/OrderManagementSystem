package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.CustomerService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.ProductService;
import com.ordermanagementsystem.dev.service.UserService;

@Controller
public class HomeController {
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;

    public HomeController(UserService userService, OrderService orderService,
            ProductService productService, CustomerService customerService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.customerService = customerService;
    }

    @GetMapping("/")
    public String home(Authentication auth, Model model) {
        UsersData userData = userService.getUserData(auth.getName());
        model.addAttribute("userData", userData);

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (isOwner) {
            model.addAttribute("stats", new DashboardStats(
                    orderService.countOrders(),
                    orderService.countOrdersByStatus("PENDING"),
                    productService.countLowStock(5), // stok <= 5
                    customerService.countCustomers()));
        } else {
            model.addAttribute("recentOrders", orderService.getRecentOrdersByCustomerEmail(auth.getName()));
        }

        return "home"; // her iki rol de aynı şablon
    }

    public record DashboardStats(long totalOrders, long pendingOrders,
            long lowStockProducts, long totalCustomers) {
    }
}
