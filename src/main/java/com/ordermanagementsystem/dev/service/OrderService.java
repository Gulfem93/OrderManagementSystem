package com.ordermanagementsystem.dev.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.OrdersData;
import com.ordermanagementsystem.dev.model.CustomerModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.repository.CustomerRepository;
import com.ordermanagementsystem.dev.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public OrdersModel createOrder(OrdersData ordersData) {
        // 1) Müşteri gerçekten var mı?
        CustomerModel customer = customerRepository.findById(ordersData.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found: " + ordersData.getCustomerId()));

        // 2) Siparişi oluştur – durum ve tarihi sunucu belirler
        OrdersModel order = new OrdersModel();
        order.setCustomer(customer);
        order.setOrderStatus("PENDING"); // enum kullanıyorsan: OrderStatus.PENDING
        order.setOrderDate(LocalDateTime.now()); // alan LocalDate ise: LocalDate.now()

        // 3) Kaydet ve döndür
        return orderRepository.save(order);
    }

    // Müşterinin siparişleri (My Orders sayfası için)
    public List<OrdersModel> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomer_CustomerId(customerId);
    }
}