package com.ordermanagementsystem.dev.model;

import com.ordermanagementsystem.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Tüm kullanıcılar (customer, owner, admin) bu tabloda; siparişler de doğrudan buraya bağlanır.
@Entity
@Table(name = "users")
@Getter
@Setter
public class UsersModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Email field cannot be blank!")
    @Email(message = "Invalid email address!")
    @Column
    private String userEmail;

    @NotBlank(message = "Password field cannot be blank!")
    @Column
    private String userPassword;

    @Column
    private String userName;

    @Column
    private String userSurname;

    // Tek rol alanı; giriş ve yetkilendirme bunu kullanır (ROLE_CUSTOMER / ROLE_OWNER / ROLE_ADMIN)
    @Convert(converter = UserRoleConverter.class)
    @Column(name = "user_role", length = 20)
    private UserRole userRole = UserRole.CUSTOMER;

    @Column
    private Boolean userActive;

    @Column
    private String userPhone;

    // Sadece OWNER'larda dolu: owner'ın şirketi
    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyModel company;

    // Şablonlar için kısayol (veritabanında saklanmaz)
    public String getCompanyName() {
        return company == null ? null : company.getCompanyName();
    }

    public Long getCompanyId() {
        return company == null ? null : company.getCompanyId();
    }

    public boolean hasRole(UserRole role) {
        return userRole == role;
    }
}
