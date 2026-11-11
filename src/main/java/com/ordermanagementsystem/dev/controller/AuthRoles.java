package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;

import com.ordermanagementsystem.enums.UserRole;

// Controller'ların ortak rol kontrolü (Spring Security yetkisi: ROLE_<ad>)
final class AuthRoles {

    private AuthRoles() {
    }

    static boolean has(Authentication auth, UserRole role) {
        String authority = "ROLE_" + role.name();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    static boolean isOwner(Authentication auth) {
        return has(auth, UserRole.OWNER);
    }

    static boolean isAdmin(Authentication auth) {
        return has(auth, UserRole.ADMIN);
    }
}
