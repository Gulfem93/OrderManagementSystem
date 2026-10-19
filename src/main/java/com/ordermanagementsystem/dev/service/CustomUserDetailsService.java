package com.ordermanagementsystem.dev.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        String normalizedMail = userEmail == null ? "" : userEmail.trim().toLowerCase(Locale.ROOT);

        UserModel user = userRepository.findByUserEmail(normalizedMail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getUserEmail())
                .password(user.getUserPassword())
                .roles("USER")
                .build();
    }
}
