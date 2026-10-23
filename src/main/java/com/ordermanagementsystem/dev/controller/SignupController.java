package com.ordermanagementsystem.dev.controller;

import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.dev.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ordermanagementsystem.dev.data.UsersData;

@Controller
public class SignupController {
    private final UserRepository userRepository;
    private final UserService userService;

    SignupController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String postSignupPage(
            @Valid @ModelAttribute("userData") UsersData userData, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }
        try {
            userService.register(userData);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("userEmail", "duplicate", e.getMessage());
            return "signup";
        }
        return "redirect:/login?registered";
    }

}
