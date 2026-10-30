package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ordermanagementsystem.dev.controller.HomeController.DashboardStats;
import com.ordermanagementsystem.dev.data.ProductsData;
import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.CustomerService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.ProductService;
import com.ordermanagementsystem.dev.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final String ROLE_OWNER = "ROLE_OWNER";

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final CustomerService customerService;

    public ProductController(ProductService productService,
            UserService userService,
            OrderService orderService,
            CustomerService customerService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    // Owner: ürün ekleme formu + yönetim listesi + istatistikler.
    // Customer: ürün listesi + kendi son siparişleri.
    @GetMapping
    public String getProductsPage(Authentication auth, Model model) {
        model.addAttribute("productData", new ProductsData());
        fillPage(auth, model);
        return "products";
    }

    // Sadece OWNER (SecurityConfig'de POST /products OWNER'a kısıtlı)
    @PostMapping
    public String addProduct(@Valid @ModelAttribute("productData") ProductsData productData,
            BindingResult bindingResult,
            Authentication auth,
            Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                String company = userService.getUserData(auth.getName()).getCompanyName();
                productService.addProduct(productData, company);
                return "redirect:/products?added";
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("productCode", "duplicate", e.getMessage());
            }
        }
        // Hata varsa formu girilen değerlerle tekrar göster
        fillPage(auth, model);
        return "products";
    }

    private boolean isOwner(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_OWNER.equals(a.getAuthority()));
    }

    // GET ve POST (hata durumu) için sayfanın ihtiyaç duyduğu tüm model verisi
    private void fillPage(Authentication auth, Model model) {
        boolean owner = isOwner(auth);
        UsersData userData = userService.getUserData(auth.getName());

        if (owner) {
            // Owner sadece kendi şirketinin ürünlerini yönetir
            String company = userData.getCompanyName();
            model.addAttribute("stats", new DashboardStats(
                    orderService.countOrders(),
                    orderService.countOrdersByStatus("PENDING"),
                    productService.countLowStock(company, LOW_STOCK_THRESHOLD),
                    customerService.countCustomers()));
            model.addAttribute("products", productService.findByCompany(company));
        } else {
            // Customer ürünleri şirket adına göre gruplanmış görür
            model.addAttribute("recentOrders",
                    orderService.getRecentOrdersByCustomerEmail(auth.getName()));
            model.addAttribute("productsByCompany", productService.findAllGroupedByCompany());
        }

        model.addAttribute("userData", userData);
        model.addAttribute("isOwner", owner);
        model.addAttribute("lowStock", LOW_STOCK_THRESHOLD);
    }
}
