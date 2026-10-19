package com.ordermanagementsystem.dev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.ordermanagementsystem.dev.data.UserData;
import com.ordermanagementsystem.dev.service.UserService;

@Controller
public class SignupController {
    private final UserService userService;

    public SignupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String getSignupPage(Model model) {
        model.addAttribute("signupForm", new UserData());
        return "signup";
    }

    @PostMapping("/signup")
    public String postSignupPage(@ModelAttribute("signupForm") UserData signupForm, Model model) {
        try {
            userService.register(signupForm.getUserName(), signupForm.getUserEmail(), signupForm.getUserPassword());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
        return "redirect:/login?registered";
    }

}
