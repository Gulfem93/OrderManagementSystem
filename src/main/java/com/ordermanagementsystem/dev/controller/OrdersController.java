package com.ordermanagementsystem.dev.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.service.CartService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.UserService;
import com.ordermanagementsystem.enums.OrderStatus;

@Controller
public class OrdersController {
    private static final String ROLE_OWNER = "ROLE_OWNER";

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    public OrdersController(OrderService orderService, UserService userService, CartService cartService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
    }

    // Owner: şirketinin ürünlerini içeren siparişler (durum filtresiyle). Customer: sepet + kendi siparişleri.
    @GetMapping("/orders")
    public String getOrderPage(@RequestParam(required = false) OrderStatus status,
            Authentication auth, Model model) {
        boolean owner = isOwner(auth);
        UsersData userData = userService.getUserData(auth.getName());

        model.addAttribute("userData", userData);
        model.addAttribute("isOwner", owner);

        if (owner) {
            model.addAttribute("orders", orderService.getOrderViewsForCompany(userData.getCompanyId(), status));
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("orders", orderService.getOrderViewsForCustomer(auth.getName()));
            model.addAttribute("trackSteps", OrderStatus.TRACK_STEPS);

            // "Sepete ekle" ile eklenen ürünler
            List<CartItemModel> cartItems = cartService.getCartItems(auth.getName());
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(cartItems));
        }
        return "orders";
    }

    // Customer > Order History: bekleyen siparişini iptal eder, ürünler stoğa geri döner
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(orderId, auth.getName());
            return "redirect:/orders?cancelled";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/orders";
        }
    }

    // Customer > Order History: kargodaki siparişi teslim aldım → Delivered
    @PostMapping("/orders/{orderId}/received")
    public String confirmReceived(@PathVariable Long orderId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.confirmReceived(orderId, auth.getName());
            redirectAttributes.addFlashAttribute("statusUpdated",
                    "#" + orderId + " numaralı siparişi teslim aldınız. İyi günlerde kullanın!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/orders";
    }

    // Owner > Orders: sipariş durumunu günceller (sadece OWNER, SecurityConfig: POST /orders/**)
    @PostMapping("/orders/{orderId}/status")
    public String updateStatus(@PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Long companyId = userService.getUserData(auth.getName()).getCompanyId();
            orderService.updateStatus(orderId, status, companyId);
            redirectAttributes.addFlashAttribute("statusUpdated",
                    "#" + orderId + " numaralı sipariş: " + status.getLabel());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/orders";
    }

    private boolean isOwner(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_OWNER.equals(a.getAuthority()));
    }
}
