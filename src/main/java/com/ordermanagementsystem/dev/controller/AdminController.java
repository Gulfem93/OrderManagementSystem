package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordermanagementsystem.dev.service.AdminService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.ProductService;
import com.ordermanagementsystem.dev.service.UserService;
import com.ordermanagementsystem.enums.OrderStatus;

// /admin/** sadece ROLE_ADMIN (SecurityConfig)
@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    public AdminController(AdminService adminService, UserService userService,
            OrderService orderService, ProductService productService) {
        this.adminService = adminService;
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    public String adminPanel(Authentication auth, Model model) {
        model.addAttribute("userData", userService.getUserData(auth.getName()));
        model.addAttribute("currentEmail", auth.getName());
        model.addAttribute("users", adminService.listUsers());
        model.addAttribute("roles", AdminService.ROLES);
        model.addAttribute("stats", new AdminStats(
                adminService.countUsers(),
                orderService.countOrders(),
                orderService.countOrdersByStatus(OrderStatus.PENDING),
                productService.countLowStock(5)));
        return "admin";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id, @RequestParam String role,
            Authentication auth, RedirectAttributes redirect) {
        try {
            adminService.updateRole(id, role, auth.getName());
            redirect.addFlashAttribute("success", "User role updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        try {
            boolean active = adminService.toggleActive(id, auth.getName());
            redirect.addFlashAttribute("success", active ? "User activated." : "User deactivated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    public record AdminStats(long totalUsers, long totalOrders, long pendingOrders, long lowStockProducts) {
    }
}
