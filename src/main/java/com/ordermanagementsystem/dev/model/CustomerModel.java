package com.ordermanagementsystem.dev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class CustomerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 100, message = "Email address must not exceed 100 characters.")
    @Column(nullable = false, unique = true, length = 100)
    private String custumerEmail;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 100, message = "Password address must not exceed 6 characters.")
    private Long customerPassword;

    @Column
    private String customerName;

    @Column
    private String customerSurname;

    @Column
    private Long customerPhone;
}
