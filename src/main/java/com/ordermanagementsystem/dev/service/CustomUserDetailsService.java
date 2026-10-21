package com.ordermanagementsystem.dev.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.model.UserModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class CustomUserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel loadUserByUserName(String email) throws UsernameNotFoundException{
        UserModel user = userRepository.findByUserEmail(email).orElseThrow(()->new UsernameNotFoundException("Kullanıcı bulunamadı"));

        return user.getUserEmail()

    }

}
