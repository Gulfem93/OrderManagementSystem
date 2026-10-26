package com.ordermanagementsystem.dev.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsersModel register(UsersData userData) {
        if (userRepository.existsByUserEmail(userData.getUserEmail())) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }

        UsersModel usermodel = new UsersModel();
        usermodel.setUserEmail(userData.getUserEmail());
        usermodel.setUserName(userData.getUserName());
        usermodel.setUserPassword(passwordEncoder.encode(userData.getUserPassword()));
        usermodel.setUserSurname(userData.getUserSurname());
        usermodel.setUserPhone(userData.getUserPhone());
        if (userData.getUserRole() == null || userData.getUserRole().isBlank()) {
            usermodel.setUserRole("USER");
        } else {
            usermodel.setUserRole(userData.getUserRole());
        }

        usermodel.setUserActive(true);

        return userRepository.save(usermodel);
    }
}
