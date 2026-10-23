package com.ordermanagementsystem.dev.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class CustomerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Email field cannot be blank!")
    @Email(message = "Invalid email address!")
    @Column
    private String customerEmail;

    @Column
    private String customerName;

    @Column
    private String customerSurname;

    @Column
    private String customerPhone;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<OrdersModel> orders = new ArrayList<>();

}
