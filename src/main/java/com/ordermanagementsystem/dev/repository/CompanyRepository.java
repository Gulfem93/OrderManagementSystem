package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.CompanyModel;

public interface CompanyRepository extends JpaRepository<CompanyModel, Long> {
    Optional<CompanyModel> findByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCase(String companyName);
}
