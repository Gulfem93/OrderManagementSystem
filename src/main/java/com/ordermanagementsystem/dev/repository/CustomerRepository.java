package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.CustomerModel;

public interface CustomerRepository extends JpaRepository<CustomerModel, Long> {
    Optional<CustomerModel> findByCustomerEmail(String customerEmail);

    boolean existsByCustomerEmail(String customerEmail);
}
