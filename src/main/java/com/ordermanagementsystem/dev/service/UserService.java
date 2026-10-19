package com.ordermanagementsystem.dev.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserModel register(String username, String userMail, String userPassword) {
        if (username == null || username.isBlank()
                || userMail == null || userMail.isBlank()
                || userPassword == null || userPassword.isBlank()) {
            throw new IllegalArgumentException("Username, email and password are required");
        }

        String normalizedUsername = username.trim();
        String normalizedMail = userMail.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUserName(normalizedUsername)) {
            throw new IllegalArgumentException("This username is already taken");
        }
        if (userRepository.existsByUserEmail(normalizedMail)) {
            throw new IllegalArgumentException("This email is already registered");
        }

        UserModel user = new UserModel();
        user.setUserName(normalizedUsername);
        user.setUserEmail(normalizedMail);
        user.setUserPassword(passwordEncoder.encode(userPassword));

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("This username or email is already registered", e);
        }
    }

}
