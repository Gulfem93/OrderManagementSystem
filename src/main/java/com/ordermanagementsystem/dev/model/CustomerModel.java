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
@Table(name = "customer")
@Getter
@Setter
public class CustomerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "The email field cannot be left blank!")
    @Email(message = "The email field cannot be left blank!")
    @Column
    private String customerEmail;

    @Column
    private String customerName;

    @Column
    private String customerSurname;

    @Column
    private Long customerPhone;

}
