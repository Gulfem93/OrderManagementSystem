package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;

import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.OrderItemModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.enums.OrderStatus;
import com.ordermanagementsystem.enums.UserRole;

// Servis testleri için küçük nesne üreticileri
final class TestData {

    private TestData() {
    }

    static CompanyModel company(long id, String name) {
        CompanyModel company = new CompanyModel();
        company.setCompanyId(id);
        company.setCompanyName(name);
        return company;
    }

    static UsersModel customer(String email) {
        UsersModel user = new UsersModel();
        user.setUserId(100L);
        user.setUserEmail(email);
        user.setUserRole(UserRole.CUSTOMER);
        user.setUserActive(true);
        return user;
    }

    static ProductsModel product(long id, CompanyModel company, long stock, String price) {
        ProductsModel product = new ProductsModel();
        product.setProductId(id);
        product.setProductName("Product " + id);
        product.setProductCode("P" + id);
        product.setProductStock(stock);
        product.setProductPrice(new BigDecimal(price));
        product.setCompany(company);
        return product;
    }

    static CartItemModel cartItem(long id, UsersModel customer, ProductsModel product, int quantity) {
        CartItemModel item = new CartItemModel();
        item.setCartItemId(id);
        item.setCustomer(customer);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    // Her kalem 2 adet
    static OrdersModel order(long id, UsersModel customer, OrderStatus status, ProductsModel... products) {
        OrdersModel order = new OrdersModel();
        order.setOrderId(id);
        order.setCustomer(customer);
        order.setOrderStatus(status);
        for (ProductsModel product : products) {
            OrderItemModel item = new OrderItemModel();
            item.setOrder(order);
            item.setProduct(product);
            item.setOrderitemQuantity(2L);
            item.setOrderitemUnitPrice(product.getProductPrice());
            order.getItems().add(item);
        }
        return order;
    }
}
