package com.ordermanagementsystem.dev.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private UsersModel customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductsModel product;

    @Column(nullable = false)
    private Integer quantity;

    // Not stored in the database; unit price * quantity
    public BigDecimal getLineTotal() {
        if (product == null || product.getProductPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return product.getProductPrice().multiply(BigDecimal.valueOf(quantity));
    }
}