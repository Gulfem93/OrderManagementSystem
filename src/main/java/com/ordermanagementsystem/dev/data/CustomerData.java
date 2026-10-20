package com.ordermanagementsystem.dev.data;

import lombok.Data;

@Data
public class CustomerData {
    private String customerEmail;
    private String customerName;
    private String customerSurname;
    private Long customerPhone;
}
