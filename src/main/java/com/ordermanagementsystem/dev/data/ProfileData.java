package com.ordermanagementsystem.dev.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Profile sayfasındaki "Personal Information" formu (e-posta, rol ve şirket buradan değiştirilmez)
@Data
public class ProfileData {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name is too long")
    private String userName;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname is too long")
    private String userSurname;

    @Size(max = 20, message = "Phone number is too long")
    private String userPhone;
}
