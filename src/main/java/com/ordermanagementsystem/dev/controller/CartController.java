package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    // My Orders > Sepetim: adet artır
    @PostMapping("/{cartItemId}/increase")
    public String increase(@PathVariable Long cartItemId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        return changeQuantity(cartItemId, 1, auth, redirectAttributes);
    }

    // My Orders > Sepetim: adet azalt (1'den aşağı inerse ürün sepetten çıkar)
    @PostMapping("/{cartItemId}/decrease")
    public String decrease(@PathVariable Long cartItemId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        return changeQuantity(cartItemId, -1, auth, redirectAttributes);
    }

    // My Orders > Sepetim: ürünü sepetten tamamen çıkar
    @PostMapping("/{cartItemId}/remove")
    public String remove(@PathVariable Long cartItemId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItem(auth.getName(), cartItemId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/orders";
    }

    private String changeQuantity(Long cartItemId, int delta,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.changeQuantity(auth.getName(), cartItemId, delta);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/orders";
    }
}
