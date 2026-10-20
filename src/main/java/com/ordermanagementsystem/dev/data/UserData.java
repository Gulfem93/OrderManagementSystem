package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class UserData {
    private String userEmail;
    private String userPassword;
    private String userName;
    private String userSurname;
    private Boolean userActive;
    private String userRole;
    private Long userPhone;
}
