package com.ordermanagementsystem.dev.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.ProductsData;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.repository.ProductRepository;

@Service
public class ProductService {
    private static final String NO_COMPANY = "Other";
    private static final Sort BY_NAME = Sort.by("productName");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductsModel> findAll() {
        return productRepository.findAll(BY_NAME);
    }

    // Owner: sadece kendi şirketinin ürünleri
    public List<ProductsModel> findByCompany(String companyName) {
        return productRepository.findByCompanyName(companyName, BY_NAME);
    }

    // Customer: şirket adına göre (alfabetik) gruplanmış ürünler
    public Map<String, List<ProductsModel>> findAllGroupedByCompany() {
        return findAll().stream().collect(Collectors.groupingBy(
                p -> p.getCompanyName() == null || p.getCompanyName().isBlank() ? NO_COMPANY : p.getCompanyName(),
                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                Collectors.toList()));
    }

    public ProductsModel addProduct(ProductsData productsData, String companyName) {
        String code = productsData.getProductCode().trim();
        if (productRepository.existsByProductCodeAndCompanyName(code, companyName)) {
            throw new IllegalArgumentException("A product with this code already exists.");
        }

        ProductsModel product = new ProductsModel();
        product.setProductCode(code);
        product.setProductName(productsData.getProductName().trim());
        product.setProductPrice(productsData.getProductPrice());
        product.setProductStock(productsData.getProductStock());
        product.setProductCategory(productsData.getProductCategory());
        product.setCompanyName(companyName);

        return productRepository.save(product);
    }

    // Owner > Edit: formu ürünün mevcut değerleriyle doldurur
    public ProductsData getProductData(Long productId, String companyName) {
        ProductsModel product = findOwnProduct(productId, companyName);

        ProductsData data = new ProductsData();
        data.setProductCode(product.getProductCode());
        data.setProductName(product.getProductName());
        data.setProductCategory(product.getProductCategory());
        data.setProductPrice(product.getProductPrice());
        data.setProductStock(product.getProductStock());
        return data;
    }

    // Owner > Edit: sadece kendi şirketinin ürününü günceller.
    // Satır kilitlenir; aynı anda gelen bir sipariş stoğu ezmez.
    @Transactional
    public ProductsModel editProduct(Long productId, ProductsData productsData, String companyName) {
        ProductsModel product = productRepository.findByIdForUpdate(productId)
                .filter(p -> companyName != null && companyName.equals(p.getCompanyName()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        // Kod değiştiyse şirketin başka bir ürünüyle çakışmamalı
        String code = productsData.getProductCode().trim();
        productRepository.findByProductCodeAndCompanyName(code, companyName)
                .filter(other -> !other.getProductId().equals(productId))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("A product with this code already exists.");
                });

        product.setProductCode(code);
        product.setProductName(productsData.getProductName().trim());
        product.setProductCategory(productsData.getProductCategory());
        product.setProductPrice(productsData.getProductPrice());
        product.setProductStock(productsData.getProductStock());
        return product;
    }

    private ProductsModel findOwnProduct(Long productId, String companyName) {
        return productRepository.findById(productId)
                .filter(p -> companyName != null && companyName.equals(p.getCompanyName()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
    }

    public long countLowStock(long threshold) {
        return productRepository.countByProductStockLessThanEqual(threshold);
    }

    public long countLowStock(String companyName, long threshold) {
        return productRepository.countByCompanyNameAndProductStockLessThanEqual(companyName, threshold);
    }
}
