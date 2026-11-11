package com.ordermanagementsystem.dev.service;

import static com.ordermanagementsystem.dev.service.TestData.company;
import static com.ordermanagementsystem.dev.service.TestData.customer;
import static com.ordermanagementsystem.dev.service.TestData.order;
import static com.ordermanagementsystem.dev.service.TestData.product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ordermanagementsystem.dev.data.OrderView;
import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.OrderRepository;
import com.ordermanagementsystem.dev.repository.ProductRepository;
import com.ordermanagementsystem.enums.OrderStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String EMAIL = "customer@test.com";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private UsersModel customer;
    private CompanyModel acme;
    private CompanyModel globex;
    private ProductsModel laptop;

    @BeforeEach
    void setUp() {
        customer = customer(EMAIL);
        acme = company(1, "Acme");
        globex = company(2, "Globex");
        laptop = product(10, acme, 3, "1000.00");
    }

    private void stubOrder(OrdersModel order) {
        when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
    }

    private void stubLock(ProductsModel product) {
        when(productRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(product));
    }

    @Test
    void ownerMovesOrderToNextStatus() {
        OrdersModel order = order(1, customer, OrderStatus.PENDING, laptop);
        stubOrder(order);

        orderService.updateStatus(1L, OrderStatus.PREPARING, acme.getCompanyId());

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(laptop.getProductStock()).isEqualTo(3);
    }

    @Test
    void ownerCancelRestoresStock() {
        OrdersModel order = order(1, customer, OrderStatus.PREPARING, laptop);
        stubOrder(order);
        stubLock(laptop);

        orderService.updateStatus(1L, OrderStatus.CANCELLED, acme.getCompanyId());

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(laptop.getProductStock()).isEqualTo(5); // 3 + 2 adet geri eklendi
    }

    @Test
    void ownerCannotSkipSteps() {
        stubOrder(order(1, customer, OrderStatus.PENDING, laptop));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.DELIVERED, acme.getCompanyId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownerCannotTouchAnotherCompanysOrder() {
        OrdersModel order = order(1, customer, OrderStatus.PENDING, laptop);
        stubOrder(order);

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PREPARING, globex.getCompanyId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found.");
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void customerCancelsOwnPendingOrder() {
        OrdersModel order = order(1, customer, OrderStatus.PENDING, laptop);
        stubOrder(order);
        stubLock(laptop);

        orderService.cancelOrder(1L, EMAIL);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(laptop.getProductStock()).isEqualTo(5);
    }

    @Test
    void customerCannotCancelAfterApproval() {
        stubOrder(order(1, customer, OrderStatus.PREPARING, laptop));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, EMAIL))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void customerCannotCancelSomeoneElsesOrder() {
        stubOrder(order(1, customer, OrderStatus.PENDING, laptop));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "other@test.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found.");
    }

    @Test
    void customerConfirmsShippedOrderAsReceived() {
        OrdersModel order = order(1, customer, OrderStatus.SHIPPED, laptop);
        stubOrder(order);

        orderService.confirmReceived(1L, EMAIL);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void ownerSeesOnlyOwnItemsOfAnOrder() {
        // Eski (bölünmemiş) çok şirketli sipariş: owner sadece kendi kalemlerini ve toplamını görür
        ProductsModel globexPhone = product(20, globex, 3, "500.00");
        OrdersModel mixed = order(1, customer, OrderStatus.PENDING, laptop, globexPhone);
        when(orderRepository.findByCompany(acme.getCompanyId())).thenReturn(List.of(mixed));

        List<OrderView> views = orderService.getOrderViewsForCompany(acme.getCompanyId(), null);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).items()).extracting(i -> i.getProduct().getProductId()).containsExactly(10L);
        assertThat(views.get(0).total()).isEqualByComparingTo("2000.00");
    }
}
