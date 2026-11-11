package com.ordermanagementsystem.dev.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Profile sayfasındaki "Change Password" formu (kurallar kayıt formuyla aynı: 8-72 karakter)
@Data
public class PasswordChangeData {
    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String newPassword;

    @NotBlank(message = "Please repeat the new password")
    private String confirmPassword;
}
