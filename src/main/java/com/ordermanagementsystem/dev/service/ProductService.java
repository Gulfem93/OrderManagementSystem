package com.ordermanagementsystem.dev.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.data.ProductsData;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductsModel> findAll() {
        return productRepository.findAll(Sort.by("productName"));
    }

    public ProductsModel addProduct(ProductsData productsData) {
        String code = productsData.getProductCode().trim();
        if (productRepository.existsByProductCode(code)) {
            throw new IllegalArgumentException("A product with this code already exists.");
        }

        ProductsModel product = new ProductsModel();
        product.setProductCode(code);
        product.setProductName(productsData.getProductName().trim());
        product.setProductPrice(productsData.getProductPrice());
        product.setProductStock(productsData.getProductStock());

        return productRepository.save(product);
    }

    public long countLowStock(long threshold) {
        return productRepository.countByProductStockLessThanEqual(threshold);
    }
}