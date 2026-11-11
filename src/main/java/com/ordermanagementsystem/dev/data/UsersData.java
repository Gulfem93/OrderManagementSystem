package com.ordermanagementsystem.dev.data;

import com.ordermanagementsystem.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Kayıt formu ve sayfaların üst barı için kullanıcı bilgisi.
// Doğrulama kuralları sadece kayıt formunda (@Valid) çalışır.
@Data
public class UsersData {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email address")
    @Size(max = 150, message = "Email is too long")
    private String userEmail;

    // BCrypt en fazla 72 byte'ı dikkate alır
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String userPassword;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name is too long")
    private String userName;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname is too long")
    private String userSurname;

    private Boolean userActive;
    private UserRole userRole;

    @Size(max = 20, message = "Phone number is too long")
    private String userPhone;

    // Kayıtta owner'ın girdiği şirket adı; sayfalarda gösterim için
    @Size(max = 150, message = "Company name is too long")
    private String companyName;
    // Owner'ın şirketi (sorgular bununla yapılır); owner değilse null
    private Long companyId;

    public boolean isOwner() {
        return userRole == UserRole.OWNER;
    }
}
