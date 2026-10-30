package com.ordermanagementsystem.dev.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.service.CartService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.UserService;

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

    // Owner: tüm siparişler. Customer: sadece kendi siparişleri.
    @GetMapping("/orders")
    public String getOrderPage(Authentication auth, Model model) {
        boolean owner = auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_OWNER.equals(a.getAuthority()));

        model.addAttribute("userData", userService.getUserData(auth.getName()));
        model.addAttribute("isOwner", owner);
        model.addAttribute("orders", owner
                ? orderService.getAllOrders()
                : orderService.getOrdersByCustomerEmail(auth.getName()));

        // Customer: "Sepete ekle" ile eklenen ürünler
        if (!owner) {
            List<CartItemModel> cartItems = cartService.getCartItems(auth.getName());
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(cartItems));
        }
        return "orders";
    }
}
