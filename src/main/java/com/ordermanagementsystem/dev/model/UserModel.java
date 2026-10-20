package com.ordermanagementsystem.dev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "The email field cannot be left blank!")
    @Email(message = "The email field cannot be left blank!")
    @Column
    private String userEmail;

    @NotBlank(message = "The password field cannot be left blank!")
    @Column
    private String userPassword;

    @Column
    private String userName;

    @Column
    private String userRole;

    @Column
    private String userActive;

    @Column
    private Long userPhone;
}
