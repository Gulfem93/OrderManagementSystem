package com.ordermanagementsystem.dev.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// Şirket: owner'lar ve ürünler bu tabloya bağlanır (önceden her yerde metin olarak tekrar ediyordu)
@Entity
@Table(name = "companies")
@Getter
@Setter
public class CompanyModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(nullable = false, unique = true, length = 150)
    private String companyName;
}
