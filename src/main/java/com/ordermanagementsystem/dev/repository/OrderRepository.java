package com.ordermanagementsystem.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ordermanagementsystem.dev.model.OrdersModel;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<OrdersModel, Long> {
    List<OrdersModel> findByOrderStatus(String orderStatus);

    List<OrdersModel> findByCustomer_CustomerId(Long customerId);

    Long countByOrderStatus(String orderStatus);

    List<OrdersModel> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
