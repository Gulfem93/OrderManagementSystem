package com.ordermanagementsystem.dev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.ProductsModel;

public interface ProductRepository extends JpaRepository<ProductsModel, Long> {
    long countByProductStockLessThanEqual(Long productStock);

    boolean existsByProductCode(String productCode);
}
