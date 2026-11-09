package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.ProductService;
import com.ordermanagementsystem.dev.service.UserService;
import com.ordermanagementsystem.enums.OrderStatus;

@Controller
public class HomeController {
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    public HomeController(UserService userService, OrderService orderService,
            ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Authentication auth, Model model) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin";
        }

        UsersData userData = userService.getUserData(auth.getName());
        model.addAttribute("userData", userData);

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (isOwner) {
            // Owner sadece kendi şirketinin rakamlarını görür
            String company = userData.getCompanyName();
            model.addAttribute("stats", new DashboardStats(
                    orderService.countOrdersByCompany(company),
                    orderService.countOrdersByCompanyAndStatus(company, OrderStatus.PENDING),
                    productService.countLowStock(company, 5), // stok <= 5
                    orderService.countCustomersByCompany(company)));
        } else {
            model.addAttribute("recentOrders", orderService.getRecentOrdersByCustomerEmail(auth.getName()));
        }

        return "home"; // her iki rol de aynı şablon
    }

    public record DashboardStats(long totalOrders, long pendingOrders,
            long lowStockProducts, long totalCustomers) {
    }
}
