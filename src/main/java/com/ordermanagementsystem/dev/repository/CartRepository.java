package com.ordermanagementsystem.dev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;

@Repository
public interface CartRepository extends JpaRepository<CartItemModel, Long> {

    Optional<CartItemModel> findByCustomerAndProduct(UsersModel customer, ProductsModel product);

    // Sepet listesi için; ürün bilgisini de tek sorguda getirir
    @EntityGraph(attributePaths = "product")
    List<CartItemModel> findByCustomer_UserEmailOrderByCartItemIdDesc(String userEmail);
}