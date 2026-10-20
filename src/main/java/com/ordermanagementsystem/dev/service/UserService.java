package com.ordermanagementsystem.dev.service;

import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.config.PasswordConfig;
import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class UserService {
    private final PasswordConfig passwordConfig;
    private final UserModel userModel;

    public UserService(PasswordConfig passwordConfig, UserModel userModel) {
        this.passwordConfig = passwordConfig;
        this.userModel = userModel;
    }

    public String users() {
        UserRepository userRepository;
        userModel.getUserEmail();
        userModel.getUserName();
        userModel.getUserPassword();
        userModel.getUserSurname();
        userModel.getUserPhone();
        userModel.getUserPhone();
        userModel.getUserRole();

        return userRepository.saveAndFlush(userModel);
    }
}
