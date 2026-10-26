package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.CustomerService;
import com.ordermanagementsystem.dev.service.OrderService;
import com.ordermanagementsystem.dev.service.UserService;

@Controller
public class HomeController {
    final private UserService userService;
    final private CustomerService customerService;
    final private OrderService orderService;

    HomeController(UserService userService, CustomerService customerService, OrderService orderService) {
        this.userService = userService;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String getHomePage(Authentication auth, Model model) {
        UsersData userData = userService.getUserData(auth.getName());
        model.addAttribute("userData", userData);
        return "home";
    }

}
