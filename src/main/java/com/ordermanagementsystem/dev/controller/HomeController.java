package com.ordermanagementsystem.dev.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Controller
public class HomeController {
    private final UserRepository userRepository;

    HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String getHomePage(Model model, Principal principal) {
        UsersData userData = new UsersData();
        userRepository.findByUserEmail(principal.getName()).ifPresent(user -> {
            userData.setUserName(user.getUserName());
            userData.setUserSurname(user.getUserSurname());
            userData.setUserEmail(user.getUserEmail());
            userData.setUserRole(user.getUserRole());
        });
        model.addAttribute("userData", userData);
        return "home";
    }

}
