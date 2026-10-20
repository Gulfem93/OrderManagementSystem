package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordermanagementsystem.dev.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUserEmail(String userEmail);

    Optional<UserModel> findByUserName(String userName);

    boolean username(String username);

    boolean useremail(String useremail);
}
