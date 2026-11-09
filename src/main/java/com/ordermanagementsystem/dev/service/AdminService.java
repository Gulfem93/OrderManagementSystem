package com.ordermanagementsystem.dev.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class AdminService {
    public static final List<String> ROLES = List.of("ADMIN", "OWNER", "CUSTOMER");
    private static final Set<String> ALLOWED_ROLES = Set.copyOf(ROLES);

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UsersModel> listUsers() {
        return userRepository.findAllByOrderByUserIdAsc();
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public void updateRole(Long userId, String newRole, String currentAdminEmail) {
        String role = newRole == null ? "" : newRole.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        UsersModel user = findUser(userId);
        if (user.getUserEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }
        if ("OWNER".equals(role) && (user.getCompanyName() == null || user.getCompanyName().isBlank())) {
            throw new IllegalArgumentException("This user has no company name, so they cannot be an OWNER.");
        }
        if (isActiveAdmin(user) && !"ADMIN".equals(role)) {
            ensureNotLastAdmin();
        }

        user.setUserRole(role);
        userRepository.save(user);
    }

    @Transactional
    public boolean toggleActive(Long userId, String currentAdminEmail) {
        UsersModel user = findUser(userId);
        if (user.getUserEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }

        boolean nowActive = !Boolean.TRUE.equals(user.getUserActive());
        if (!nowActive && isActiveAdmin(user)) {
            ensureNotLastAdmin();
        }

        user.setUserActive(nowActive);
        userRepository.save(user);
        return nowActive;
    }

    private UsersModel findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private boolean isActiveAdmin(UsersModel user) {
        return "ADMIN".equals(user.getUserRole()) && Boolean.TRUE.equals(user.getUserActive());
    }

    private void ensureNotLastAdmin() {
        if (userRepository.countByUserRoleAndUserActiveTrue("ADMIN") <= 1) {
            throw new IllegalArgumentException("At least one active admin must remain.");
        }
    }
}
