package com.ordermanagementsystem.dev.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.ProductsModel;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductsModel, Long> {
    // Ürün koduna göre tek ürün getirir
    Optional<ProductsModel> findByProductCode(String productCode);

    // Yeni ürün eklerken aynı kod var mı kontrolü
    boolean existsByProductCode(String productCode);

    // Owner dashboard'daki "Low Stock Products" için
    long countByStockLessThanEqual(int threshold);

    // Düşük stoklu ürünlerin listesi (örn. uyarı sayfası)
    List<ProductsModel> findByStockLessThanEqual(int threshold);

}
