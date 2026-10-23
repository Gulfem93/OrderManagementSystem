package com.ordermanagementsystem.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ordermanagementsystem.dev.model.ProductsModel;

@Repository
public interface OrdersRepository extends JpaRepository<ProductsModel, Long> {

}
