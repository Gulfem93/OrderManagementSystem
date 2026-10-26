package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ordermanagementsystem.dev.data.ProductsData;
import com.ordermanagementsystem.dev.service.ProductService;
import com.ordermanagementsystem.dev.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final long LOW_STOCK_THRESHOLD = 5;

    private final ProductService productService;
    private final UserService userService;

    ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    // Owner: ürün ekleme formu + yönetim listesi. Customer: sadece ürün listesi.
    @GetMapping
    public String getProductsPage(Authentication auth, Model model) {
        model.addAttribute("productData", new ProductsData());
        fillPage(auth, model);
        return "products";
    }

    // Sadece OWNER (SecurityConfig'de POST /products OWNER'a kısıtlı)
    @PostMapping
    public String addProduct(@Valid @ModelAttribute("productData") ProductsData productData,
            BindingResult bindingResult, Authentication auth, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                productService.addProduct(productData);
                return "redirect:/products?added";
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("productCode", "duplicate", e.getMessage());
            }
        }
        // Hata varsa formu girilen değerlerle tekrar göster
        fillPage(auth, model);
        return "products";
    }

    private void fillPage(Authentication auth, Model model) {
        model.addAttribute("userData", userService.getUserData(auth.getName()));
        model.addAttribute("isOwner", auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_OWNER".equals(a.getAuthority())));
        model.addAttribute("products", productService.findAll());
        model.addAttribute("lowStock", LOW_STOCK_THRESHOLD);
    }
}