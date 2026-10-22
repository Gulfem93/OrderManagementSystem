package com.ordermanagementsystem.dev.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.data.UserData;
import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserModel register(UserData userData) {
        if (userRepository.existsByUserEmail(userData.getUserEmail())) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }

        UserModel usermodel = new UserModel();
        usermodel.setUserEmail(userData.getUserEmail());
        usermodel.setUserName(userData.getUserName());
        usermodel.setUserPassword(passwordEncoder.encode(userData.getUserPassword()));
        usermodel.setUserSurname(userData.getUserSurname());
        usermodel.setUserPhone(userData.getUserPhone());
        if (userData.getUserRole().isEmpty()) {
            usermodel.setUserRole("USER");
        } else {
            usermodel.setUserRole(userData.getUserRole());
        }

        usermodel.setUserActive(true);

        return userRepository.save(usermodel);
    }
}
