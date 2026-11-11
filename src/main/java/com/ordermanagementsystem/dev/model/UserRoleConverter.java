package com.ordermanagementsystem.dev.model;

import com.ordermanagementsystem.enums.UserRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// users.user_role metin olarak saklanır ("OWNER", "CUSTOMER", "ADMIN").
// Eski/bozuk değerler (boş, "USER", "ROLE_OWNER") uygulamayı çökertmez:
// tanınmayan değer CUSTOMER kabul edilir.
@Converter
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        UserRole role = UserRole.fromValue(value);
        return role == null ? UserRole.CUSTOMER : role;
    }
}
