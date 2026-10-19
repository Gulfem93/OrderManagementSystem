package com.ordermanagementsystem.dev.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderController {
    @GetMapping("/orders")
    public String getOrdersPage(Principal principal, Model model) {
        model.addAttribute("userEmail", principal.getName());
        return "orders";
    }

}
