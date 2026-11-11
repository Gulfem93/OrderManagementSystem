package com.ordermanagementsystem.dev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.enums.UserRole;

@Repository
public interface UserRepository extends JpaRepository<UsersModel, Long> {
    Optional<UsersModel> findByUserEmail(String userEmail);

    Optional<UsersModel> findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByUserEmail(String userEmail);

    List<UsersModel> findAllByOrderByUserIdAsc();

    long countByUserRoleAndUserActiveTrue(UserRole userRole);
}
