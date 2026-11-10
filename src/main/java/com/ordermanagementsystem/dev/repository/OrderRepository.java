package com.ordermanagementsystem.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.enums.OrderStatus;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<OrdersModel, Long> {
    List<OrdersModel> findByOrderStatus(OrderStatus orderStatus);

    Long countByOrderStatus(OrderStatus orderStatus);

    List<OrdersModel> findTop5ByCustomer_UserEmailOrderByOrderDateDesc(String userEmail);

    // Order History için müşteri, kalemler ve ürünler tek sorguda gelir (detay satırı için)
    @EntityGraph(attributePaths = { "customer", "items", "items.product" })
    List<OrdersModel> findByCustomer_UserEmailOrderByOrderDateDesc(String userEmail);

    @EntityGraph(attributePaths = { "customer", "items", "items.product" })
    List<OrdersModel> findAllByOrderByOrderDateDesc();

    // Owner: içinde şirketinin ürünü olan siparişler, en yeni üstte
    @EntityGraph(attributePaths = { "customer", "items", "items.product" })
    @Query("select distinct o from OrdersModel o join o.items i "
            + "where i.product.company.companyId = :companyId order by o.orderDate desc")
    List<OrdersModel> findByCompany(@Param("companyId") Long companyId);

    @Query("select count(distinct o) from OrdersModel o join o.items i where i.product.company.companyId = :companyId")
    long countByCompany(@Param("companyId") Long companyId);

    @Query("select count(distinct o) from OrdersModel o join o.items i "
            + "where i.product.company.companyId = :companyId and o.orderStatus = :status")
    long countByCompanyAndStatus(@Param("companyId") Long companyId, @Param("status") OrderStatus status);

    @Query("select count(distinct o.customer) from OrdersModel o join o.items i where i.product.company.companyId = :companyId")
    long countCustomersByCompany(@Param("companyId") Long companyId);

    List<OrdersModel> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
