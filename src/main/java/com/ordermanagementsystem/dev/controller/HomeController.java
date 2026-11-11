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
        if (AuthRoles.isAdmin(auth)) {
            return "redirect:/admin";
        }

        UsersData userData = userService.getUserData(auth.getName());
        model.addAttribute("userData", userData);

        if (AuthRoles.isOwner(auth)) {
            // Owner sadece kendi şirketinin rakamlarını görür
            Long companyId = userData.getCompanyId();
            model.addAttribute("stats", new DashboardStats(
                    orderService.countOrdersByCompany(companyId),
                    orderService.countOrdersByCompanyAndStatus(companyId, OrderStatus.PENDING),
                    productService.countLowStock(companyId),
                    orderService.countCustomersByCompany(companyId)));
        } else {
            model.addAttribute("recentOrders", orderService.getRecentOrdersByCustomerEmail(auth.getName()));
        }

        return "home"; // her iki rol de aynı şablon
    }

    public record DashboardStats(long totalOrders, long pendingOrders,
            long lowStockProducts, long totalCustomers) {
    }
}
