package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;

import com.ordermanagementsystem.enums.CategoryType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductsData {
    @NotBlank(message = "Product code cannot be blank!")
    private String productCode;

    @NotBlank(message = "Product name cannot be blank!")
    private String productName;

    // Seçim kutusundan gelen enum adı (ör. "LAPTOP") otomatik CategoryType'a çevrilir
    @NotNull(message = "Please select a category!")
    private CategoryType productCategory;

    @NotNull(message = "Price is required!")
    @PositiveOrZero(message = "Price cannot be negative!")
    private BigDecimal productPrice;

    @NotNull(message = "Stock is required!")
    @PositiveOrZero(message = "Stock cannot be negative!")
    private Long productStock;
}
