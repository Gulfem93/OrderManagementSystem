package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUserName(String userName);

    Optional<UserModel> findByUserEmail(String userEmail);

    boolean existsByUserName(String userName);

    boolean existsByUserEmail(String userEmail);
}
