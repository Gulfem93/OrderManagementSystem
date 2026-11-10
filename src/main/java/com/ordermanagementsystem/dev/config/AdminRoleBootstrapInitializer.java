package com.ordermanagementsystem.dev.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

/**
 * Uygulama hazır olduğunda:
 * - Rolü boş olan kullanıcılara varsayılan rol (CUSTOMER) atanır.
 * - security.admin.emails (veya ADMIN_EMAILS ortam değişkeni) içindeki
 *   e-posta adreslerine sahip kayıtlı kullanıcılar ADMIN yapılır.
 *   Birden fazla adres virgülle ayrılır: "a@mail.com, b@mail.com"
 *
 * Bu projede giriş e-posta ile yapıldığı için admin kullanıcıları
 * userName yerine userEmail ile eşleştirilir (userName benzersiz değildir).
 */
@Component
public class AdminRoleBootstrapInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRoleBootstrapInitializer.class);

    private final UserRepository userRepository;
    private final String adminEmailsProperty;

    public AdminRoleBootstrapInitializer(UserRepository userRepository,
            @Value("${security.admin.emails:${ADMIN_EMAILS:}}") String adminEmailsProperty) {
        this.userRepository = userRepository;
        this.adminEmailsProperty = adminEmailsProperty;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void bootstrapRoles() {
        List<UsersModel> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }

        boolean changed = false;
        for (UsersModel user : users) {
            if (user.getUserRole() == null) {
                user.setUserRole(UserRole.CUSTOMER);
                changed = true;
            }
        }

        Set<String> adminEmails = parseAdminEmails(adminEmailsProperty);
        if (!adminEmails.isEmpty()) {
            Map<String, UsersModel> userByEmail = users.stream()
                    .filter(user -> StringUtils.hasText(user.getUserEmail()))
                    .collect(Collectors.toMap(
                            user -> user.getUserEmail().toLowerCase(Locale.ROOT),
                            Function.identity(),
                            (left, right) -> left));

            for (String adminEmail : adminEmails) {
                UsersModel user = userByEmail.get(adminEmail);
                if (user == null) {
                    LOGGER.warn("Admin bootstrap email is not found in persisted users: {}", adminEmail);
                    continue;
                }
                if (user.getUserRole() != UserRole.ADMIN) {
                    user.setUserRole(UserRole.ADMIN);
                    changed = true;
                }
            }
        }

        if (changed) {
            userRepository.saveAll(users);
            LOGGER.info("User role bootstrap completed.");
        }
    }

    private Set<String> parseAdminEmails(String value) {
        if (!StringUtils.hasText(value)) {
            return Set.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
