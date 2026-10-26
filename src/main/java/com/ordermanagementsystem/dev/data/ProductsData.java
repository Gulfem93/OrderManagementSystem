package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;

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

    @NotNull(message = "Price is required!")
    @PositiveOrZero(message = "Price cannot be negative!")
    private BigDecimal productPrice;

    @NotNull(message = "Stock is required!")
    @PositiveOrZero(message = "Stock cannot be negative!")
    private Long productStock;
}
