package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class ProductData {
    // productName, productPrice, productStock, productCode
    private String productCode;
    private String productName;
    private Long productPrice;
    private Long productStock;

}
