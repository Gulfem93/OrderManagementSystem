package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class CustomerData {
    private String custumerEmail;
    private Long customerPassword;
    private String customerName;
    private String customerSurname;
    private Long customerPhone;
}
