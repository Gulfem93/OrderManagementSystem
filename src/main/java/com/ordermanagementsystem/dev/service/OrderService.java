package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.OrderView;
import com.ordermanagementsystem.dev.model.OrderItemModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.repository.OrderRepository;
import com.ordermanagementsystem.dev.repository.ProductRepository;
import com.ordermanagementsystem.enums.OrderStatus;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // Owner: tüm siparişler (Orders sayfası için), en yeni üstte
    public List<OrdersModel> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    // Customer: kendi siparişleri (My Orders sayfası için), en yeni üstte
    public List<OrdersModel> getOrdersByCustomerEmail(String customerEmail) {
        return orderRepository.findByCustomer_UserEmailOrderByOrderDateDesc(customerEmail);
    }

    // Ana sayfadaki "Recent Orders" tablosu için
    public List<OrdersModel> getRecentOrdersByCustomerEmail(String customerEmail) {
        return orderRepository.findTop5ByCustomer_UserEmailOrderByOrderDateDesc(customerEmail);
    }

    // Customer: Order History (siparişin tüm kalemleri)
    public List<OrderView> getOrderViewsForCustomer(String customerEmail) {
        return getOrdersByCustomerEmail(customerEmail).stream()
                .map(order -> toView(order, order.getItems()))
                .toList();
    }

    // Owner: şirketinin ürünlerini içeren siparişler; sadece kendi kalemleri ve onların toplamı.
    // status null ise tüm durumlar gelir.
    public List<OrderView> getOrderViewsForCompany(Long companyId, OrderStatus status) {
        return orderRepository.findByCompany(companyId).stream()
                .filter(order -> status == null || order.getOrderStatus() == status)
                .map(order -> toView(order, order.getItems().stream()
                        .filter(item -> item.getProduct().belongsTo(companyId))
                        .toList()))
                .toList();
    }

    private OrderView toView(OrdersModel order, List<OrderItemModel> items) {
        BigDecimal total = items.stream()
                .map(OrderItemModel::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderView(order, items, total);
    }

    // Customer: kendi bekleyen siparişini iptal eder, ürünler stoğa geri döner
    @Transactional
    public void cancelOrder(Long orderId, String customerEmail) {
        OrdersModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!order.getCustomer().getUserEmail().equalsIgnoreCase(customerEmail)) {
            throw new IllegalArgumentException("Order not found.");
        }
        if (order.getOrderStatus() == null || !order.getOrderStatus().isCancellable()) {
            throw new IllegalStateException("Only orders awaiting approval can be cancelled.");
        }

        restoreStock(order);
        order.setOrderStatus(OrderStatus.CANCELLED);
    }

    // Customer: kargodaki siparişini teslim aldığını onaylar → DELIVERED
    @Transactional
    public void confirmReceived(Long orderId, String customerEmail) {
        OrdersModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!order.getCustomer().getUserEmail().equalsIgnoreCase(customerEmail)) {
            throw new IllegalArgumentException("Order not found.");
        }
        if (order.getOrderStatus() == null || !order.getOrderStatus().isReceivable()) {
            throw new IllegalStateException("Only shipped orders can be marked as received.");
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
    }

    // Owner: siparişin durumunu ilerletir (Onayla → Hazırlanıyor → Kargoda → Teslim edildi).
    // Sadece şirketinin ürününü içeren siparişleri değiştirebilir; iptal/iadede stok geri eklenir.
    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus, Long companyId) {
        OrdersModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        boolean ownsItem = order.getItems().stream()
                .anyMatch(item -> item.getProduct().belongsTo(companyId));
        if (!ownsItem) {
            throw new IllegalArgumentException("Order not found.");
        }
        if (newStatus == null || order.getOrderStatus() == null || !order.getOrderStatus().canChangeTo(newStatus)) {
            throw new IllegalStateException("This order cannot be changed from "
                    + (order.getOrderStatus() == null ? "-" : order.getOrderStatus().getLabel())
                    + " to " + (newStatus == null ? "-" : newStatus.getLabel()) + ".");
        }

        if (newStatus.restoresStock()) {
            restoreStock(order);
        }
        order.setOrderStatus(newStatus);
    }

    // Ürün id sırasıyla kilitler (checkout ile aynı sıra; deadlock olmaz)
    private void restoreStock(OrdersModel order) {
        List<OrderItemModel> lockOrder = order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getProductId()))
                .toList();
        for (OrderItemModel item : lockOrder) {
            ProductsModel product = productRepository.findByIdForUpdate(item.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            long stock = product.getProductStock() == null ? 0 : product.getProductStock();
            product.setProductStock(stock + item.getOrderitemQuantity());
        }
    }

    // Owner dashboard: sadece şirketinin siparişleri
    public long countOrdersByCompany(Long companyId) {
        return orderRepository.countByCompany(companyId);
    }

    public long countOrdersByCompanyAndStatus(Long companyId, OrderStatus orderStatus) {
        return orderRepository.countByCompanyAndStatus(companyId, orderStatus);
    }

    public long countCustomersByCompany(Long companyId) {
        return orderRepository.countCustomersByCompany(companyId);
    }

    public long countOrders() {
        return orderRepository.count();
    }

    public long countOrdersByStatus(OrderStatus orderStatus) {
        return orderRepository.countByOrderStatus(orderStatus);
    }
}