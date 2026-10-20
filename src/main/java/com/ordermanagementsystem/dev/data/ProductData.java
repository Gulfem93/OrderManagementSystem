package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductData {
    // productName, productPrice, productStock, productCode
    private String productCode;
    private String productName;
    private BigDecimal productPrice;
    private Long productStock;

}
