package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordermanagementsystem.dev.model.UsersModel;

@Repository
public interface UserRepository extends JpaRepository<UsersModel, Long> {
    Optional<UsersModel> findByUserEmail(String userEmail);

    Optional<UsersModel> findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByUserEmail(String userEmail);
}
