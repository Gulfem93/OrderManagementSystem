package com.ordermanagementsystem.dev.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginData {
    @NotBlank(message = "Email address is required.")
    @Email(message = "Please enter a valid email address.")
    private String userEmail;

    @NotBlank(message = "Password is required.")
    private String userPassword;
}
