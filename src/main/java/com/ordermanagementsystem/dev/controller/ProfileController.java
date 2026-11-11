package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ordermanagementsystem.dev.data.PasswordChangeData;
import com.ordermanagementsystem.dev.data.ProfileData;
import com.ordermanagementsystem.dev.service.UserService;

import jakarta.validation.Valid;

// Giriş yapmış her kullanıcı (customer, owner, admin) kendi profilini görür ve düzenler
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getProfilePage(Authentication auth, Model model) {
        model.addAttribute("profileData", userService.getProfileData(auth.getName()));
        model.addAttribute("passwordData", new PasswordChangeData());
        fillPage(auth, model);
        return "profile";
    }

    // Ad, soyad, telefon
    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileData") ProfileData profileData,
            BindingResult bindingResult,
            Authentication auth,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("passwordData", new PasswordChangeData());
            fillPage(auth, model);
            return "profile";
        }
        userService.updateProfile(auth.getName(), profileData);
        return "redirect:/profile?updated";
    }

    // Şifre değiştirme
    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordData") PasswordChangeData passwordData,
            BindingResult bindingResult,
            Authentication auth,
            Model model) {
        if (passwordData.getNewPassword() != null
                && !passwordData.getNewPassword().equals(passwordData.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (!bindingResult.hasErrors()) {
            try {
                userService.changePassword(auth.getName(),
                        passwordData.getCurrentPassword(), passwordData.getNewPassword());
                return "redirect:/profile?passwordChanged";
            } catch (IllegalArgumentException e) {
                String field = e.getMessage().startsWith("Current") ? "currentPassword" : "newPassword";
                bindingResult.rejectValue(field, "invalid", e.getMessage());
            }
        }
        // Şifre alanları güvenlik için tekrar doldurulmaz
        passwordData.setCurrentPassword(null);
        passwordData.setNewPassword(null);
        passwordData.setConfirmPassword(null);
        model.addAttribute("profileData", userService.getProfileData(auth.getName()));
        fillPage(auth, model);
        return "profile";
    }

    private void fillPage(Authentication auth, Model model) {
        model.addAttribute("userData", userService.getUserData(auth.getName()));
        model.addAttribute("isOwner", AuthRoles.isOwner(auth));
        model.addAttribute("isAdmin", AuthRoles.isAdmin(auth));
    }
}
