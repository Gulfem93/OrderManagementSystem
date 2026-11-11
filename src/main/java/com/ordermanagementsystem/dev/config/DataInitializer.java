package com.ordermanagementsystem.dev.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

/**
 * app.seed.enabled=true ise uygulama açılışında admin hesabını oluşturur (zaten varsa dokunmaz).
 * Şifre koda yazılmaz: app.seed.admin-password veya SEED_ADMIN_PASSWORD ortam değişkeninden okunur.
 * Şifre verilmezse hesap oluşturulmaz.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-email:${SEED_ADMIN_EMAIL:gulfem@admin.com}}") String adminEmail,
            @Value("${app.seed.admin-password:${SEED_ADMIN_PASSWORD:}}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUserEmail(adminEmail)) {
            return;
        }
        if (!StringUtils.hasText(adminPassword)) {
            LOGGER.warn("app.seed.enabled=true but no admin password is set "
                    + "(app.seed.admin-password or SEED_ADMIN_PASSWORD); the admin account was not created.");
            return;
        }

        UsersModel admin = new UsersModel();
        admin.setUserEmail(adminEmail);
        admin.setUserName("Admin");
        admin.setUserSurname("User");
        admin.setUserPassword(passwordEncoder.encode(adminPassword));
        admin.setUserRole(UserRole.ADMIN);
        admin.setUserActive(true);
        userRepository.save(admin);
        LOGGER.info("Seed admin account created: {}", adminEmail);
    }
}
