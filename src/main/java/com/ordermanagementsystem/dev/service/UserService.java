package com.ordermanagementsystem.dev.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.config.PasswordConfig;
import com.ordermanagementsystem.dev.data.UserData;
import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final PasswordConfig passwordConfig;
    private final UserRepository userRepository;
    private final UserModel userModel;

    public UserService(
            PasswordConfig passwordConfig, UserRepository userRepository,
            UserModel userModel, PasswordEncoder passwordEncoder) {
        this.passwordConfig = passwordConfig;
        this.userRepository = userRepository;
        this.userModel = userModel;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel register(UserData userData) {
        UserModel usermodel = new UserModel();
        usermodel.setUserEmail(userData.getUserEmail());
        usermodel.setUserName(userData.getUserName());
        usermodel.setUserPassword(passwordEncoder.encode(userData.getUserPassword()));
        usermodel.setUserSurname(userData.getUserSurname());
        usermodel.setUserPhone(userData.getUserPhone());
        usermodel.setUserRole(userData.getUserRole());
        usermodel.setUserActive(userData.getUserActive());

        return userRepository.save(usermodel);
    }
}
