package com.ordermanagementsystem.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// Kullanıcının tek rol alanı (users.user_role). Spring Security'de ROLE_<ad> olarak kullanılır.
public enum UserRole {
    CUSTOMER("Customer"),
    OWNER("Owner"),
    ADMIN("Admin");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // Kayıt formunda seçilebilen roller; ADMIN sadece admin panelinden verilir
    public static List<UserRole> signupRoles() {
        return List.of(OWNER, CUSTOMER);
    }

    public boolean isSignupRole() {
        return signupRoles().contains(this);
    }

    /**
     * Metinden rol bulur: "OWNER", "owner", "ROLE_OWNER" kabul edilir.
     * Bulunamazsa null döner.
     */
    public static UserRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        if (v.startsWith("ROLE_")) {
            v = v.substring(5);
        }
        String name = v;
        return Arrays.stream(values()).filter(r -> r.name().equals(name)).findFirst().orElse(null);
    }
}
