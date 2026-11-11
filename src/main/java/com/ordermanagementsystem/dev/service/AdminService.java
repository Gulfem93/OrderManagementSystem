package com.ordermanagementsystem.dev.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.UserRole;

@Service
public class AdminService {
    public static final List<UserRole> ROLES = List.of(UserRole.values());

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
        UserRole role = UserRole.fromValue(newRole);
        if (role == null) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        UsersModel user = findUser(userId);
        if (user.getUserEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot change your own role.");
        }
        if (role == UserRole.OWNER && user.getCompany() == null) {
            throw new IllegalArgumentException("This user has no company, so they cannot be an OWNER.");
        }
        if (isActiveAdmin(user) && role != UserRole.ADMIN) {
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
        return user.getUserRole() == UserRole.ADMIN && Boolean.TRUE.equals(user.getUserActive());
    }

    private void ensureNotLastAdmin() {
        if (userRepository.countByUserRoleAndUserActiveTrue(UserRole.ADMIN) <= 1) {
            throw new IllegalArgumentException("At least one active admin must remain.");
        }
    }
}
