package com.ordermanagementsystem.dev.data;

import com.ordermanagementsystem.enums.UserRole;

import lombok.Data;

// Kayıt formu ve sayfaların üst barı için kullanıcı bilgisi
@Data
public class UsersData {
    private String userEmail;
    private String userPassword;
    private String userName;
    private String userSurname;
    private Boolean userActive;
    private UserRole userRole;
    private String userPhone;

    // Kayıtta owner'ın girdiği şirket adı; sayfalarda gösterim için
    private String companyName;
    // Owner'ın şirketi (sorgular bununla yapılır); owner değilse null
    private Long companyId;

    public boolean isOwner() {
        return userRole == UserRole.OWNER;
    }
}
