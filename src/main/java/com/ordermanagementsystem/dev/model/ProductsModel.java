package com.ordermanagementsystem.dev.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column
    private String productCode;

    @Column
    private String productName;

    @Column
    private BigDecimal productPrice;

    @Column
    private Long productStock;

    @Column
    private String productCategory;

    // Ürünü ekleyen owner'ın şirketi
    @Column
    private String companyName;

}
