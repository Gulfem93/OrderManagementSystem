package com.ordermanagementsystem.dev.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

import lombok.RequiredArgsConstructor;

// app.seed.enabled=true ise uygulama açılışında admin hesabını oluşturur (zaten varsa dokunmaz)
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "gulfem@admin.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createUserData();
    }

    private void createUserData() {
        if (userRepository.existsByUserEmail(ADMIN_EMAIL)) {
            return;
        }

        UsersModel adminGulfem = new UsersModel();
        adminGulfem.setUserEmail(ADMIN_EMAIL);
        adminGulfem.setUserName("Gülfem");
        adminGulfem.setUserSurname("Admin");
        adminGulfem.setUserPassword(passwordEncoder.encode("admin123*"));
        // Giriş ve yetkilendirme userRole alanını kullanır (CustomUserDetailsService)
        adminGulfem.setUserRole("ADMIN");
        adminGulfem.setRole(UserRole.ADMIN);
        adminGulfem.setUserActive(true);
        userRepository.save(adminGulfem);
    }
}
