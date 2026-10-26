package com.ordermanagementsystem.dev.service;

import org.springframework.stereotype.Service;

import com.ordermanagementsystem.dev.data.CustomersData;
import com.ordermanagementsystem.dev.model.CustomerModel;
import com.ordermanagementsystem.dev.repository.CustomerRepository;
import com.ordermanagementsystem.dev.repository.OrderRepository;

@Service
public class CustomerService {
    final private CustomerRepository customerRepository;
    final private OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public CustomerModel customers(CustomersData customersData) {
        if (customerRepository.existsByCustomerEmail(customersData.getCustomerEmail())) {
            throw new IllegalArgumentException("A user with this email address is already registered.");
        }
        CustomerModel cModel = new CustomerModel();
        cModel.setCustomerEmail(customersData.getCustomerEmail());
        cModel.setCustomerName(customersData.getCustomerName());
        cModel.setCustomerSurname(customersData.getCustomerSurname());
        cModel.setCustomerPhone(customersData.getCustomerPhone());

        return customerRepository.save(cModel);
    }

    public long countCustomers() {
        return customerRepository.count();
    }
}
