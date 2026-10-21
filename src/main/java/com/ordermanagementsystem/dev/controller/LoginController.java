package com.ordermanagementsystem.dev.controller;

import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

}
