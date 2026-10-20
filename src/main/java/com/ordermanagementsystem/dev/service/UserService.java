package com.ordermanagementsystem.dev.service;

import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.config.PasswordConfig;
import com.ordermanagementsystem.dev.model.UserModel;

@Service
public class UserService {
    private final PasswordConfig passwordConfig;
    private final UserModel userModel;

    public UserService userService(PasswordConfig passwordConfig, UserModel userModel) {
        this.passwordConfig = passwordConfig;
        this.userModel = userModel;
    }
}
