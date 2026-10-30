package com.ordermanagementsystem.dev.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.ProductsModel;

public interface ProductRepository extends JpaRepository<ProductsModel, Long> {
    long countByProductStockLessThanEqual(Long productStock);

    long countByCompanyNameAndProductStockLessThanEqual(String companyName, Long productStock);

    boolean existsByProductCodeAndCompanyName(String productCode, String companyName);

    List<ProductsModel> findByCompanyName(String companyName, Sort sort);
}
