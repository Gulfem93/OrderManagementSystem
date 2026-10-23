package com.ordermanagementsystem.dev.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UsersModel user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        return User.withUsername(user.getUserEmail())
                .password(user.getUserPassword())
                .roles(user.getUserRole())
                .disabled(!Boolean.TRUE.equals(user.getUserActive()))
                .build();
    }
}