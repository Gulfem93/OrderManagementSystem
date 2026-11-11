package com.ordermanagementsystem.dev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.UserService;
import com.ordermanagementsystem.enums.UserRole;

import jakarta.validation.Valid;

@Controller
public class SignupController {
    private final UserService userService;

    SignupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model) {
        model.addAttribute("userData", new UsersData());
        return "signup";
    }

    @PostMapping("/signup")
    public String postSignupPage(
            @Valid @ModelAttribute("userData") UsersData userData, BindingResult bindingResult) {
        if (userData.getUserRole() == null || !userData.getUserRole().isSignupRole()) {
            bindingResult.rejectValue("userRole", "required", "Please select a role");
        }
        if (userData.getUserRole() == UserRole.OWNER) {
            if (userData.getCompanyName() == null || userData.getCompanyName().isBlank()) {
                bindingResult.rejectValue("companyName", "required", "Company name is required for owners");
            } else if (userService.isCompanyNameTaken(userData.getCompanyName())) {
                bindingResult.rejectValue("companyName", "duplicate", "A company with this name is already registered.");
            }
        }
        if (userService.isEmailTaken(userData.getUserEmail())) {
            bindingResult.rejectValue("userEmail", "duplicate", "A user with this email address is already registered.");
        }
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            userService.register(userData);
        } catch (IllegalArgumentException e) {
            // Aynı anda iki kayıt gelirse (yarış durumu) buraya düşer
            bindingResult.reject("signup", e.getMessage());
            return "signup";
        }
        return "redirect:/login?registered";
    }
}
