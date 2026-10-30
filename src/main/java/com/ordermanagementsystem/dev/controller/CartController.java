package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ordermanagementsystem.dev.data.CartItemData;
import com.ordermanagementsystem.dev.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Ürün listesindeki "Sepete ekle" butonu buraya POST eder
    @PostMapping("/add")
    public String addToCart(@ModelAttribute CartItemData cartItemData,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(auth.getName(), cartItemData);
            return "redirect:/products?cartAdded";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/products";
        }
    }
}
