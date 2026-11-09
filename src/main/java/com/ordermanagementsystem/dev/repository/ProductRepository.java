package com.ordermanagementsystem.dev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ordermanagementsystem.dev.model.ProductsModel;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<ProductsModel, Long> {
    long countByProductStockLessThanEqual(Long productStock);

    long countByCompanyNameAndProductStockLessThanEqual(String companyName, Long productStock);

    boolean existsByProductCodeAndCompanyName(String productCode, String companyName);

    Optional<ProductsModel> findByProductCodeAndCompanyName(String productCode, String companyName);

    List<ProductsModel> findByCompanyName(String companyName, Sort sort);

    // Satın alma sırasında satırı kilitler; aynı anda iki sipariş stoğu eksiye
    // düşüremez
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductsModel p where p.productId = :id")
    Optional<ProductsModel> findByIdForUpdate(@Param("id") Long id);
}
