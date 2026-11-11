package com.ordermanagementsystem.dev.service;

import static com.ordermanagementsystem.dev.service.TestData.cartItem;
import static com.ordermanagementsystem.dev.service.TestData.company;
import static com.ordermanagementsystem.dev.service.TestData.customer;
import static com.ordermanagementsystem.dev.service.TestData.product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ordermanagementsystem.dev.data.CartItemData;
import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.model.CompanyModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CartRepository;
import com.ordermanagementsystem.dev.repository.OrderRepository;
import com.ordermanagementsystem.dev.repository.ProductRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.OrderStatus;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String EMAIL = "customer@test.com";

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CartService cartService;

    private UsersModel customer;
    private CompanyModel acme;
    private CompanyModel globex;

    @BeforeEach
    void setUp() {
        customer = customer(EMAIL);
        acme = company(1, "Acme");
        globex = company(2, "Globex");
    }

    // findByIdForUpdate, verilen ürünlerden id'ye göre cevap verir
    private void lockable(ProductsModel... products) {
        Map<Long, ProductsModel> byId = List.of(products).stream()
                .collect(Collectors.toMap(ProductsModel::getProductId, Function.identity()));
        when(productRepository.findByIdForUpdate(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(byId.get(inv.<Long>getArgument(0))));
    }

    // saveAll kendisine verilen siparişleri geri döndürür
    @SuppressWarnings("unchecked")
    private void savedOrders() {
        when(orderRepository.saveAll(any()))
                .thenAnswer(inv -> new ArrayList<>((Collection<OrdersModel>) inv.getArgument(0)));
    }

    @Test
    void checkoutDecreasesStockAndEmptiesCart() {
        ProductsModel laptop = product(10, acme, 5, "1000.00");
        List<CartItemModel> cart = List.of(cartItem(1, customer, laptop, 2));
        when(cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(EMAIL)).thenReturn(cart);
        lockable(laptop);
        savedOrders();

        List<OrdersModel> orders = cartService.checkout(EMAIL);

        assertThat(laptop.getProductStock()).isEqualTo(3);
        assertThat(orders).hasSize(1);
        OrdersModel order = orders.get(0);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getCustomer()).isSameAs(customer);
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("2000.00"));
        verify(cartRepository).deleteAll(cart);
    }

    @Test
    void checkoutCreatesOneOrderPerCompany() {
        ProductsModel acmeA = product(10, acme, 5, "10.00");
        ProductsModel globexB = product(20, globex, 5, "20.00");
        ProductsModel acmeC = product(30, acme, 5, "30.00");
        when(cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(EMAIL)).thenReturn(List.of(
                cartItem(3, customer, acmeC, 1),
                cartItem(2, customer, globexB, 1),
                cartItem(1, customer, acmeA, 1)));
        lockable(acmeA, globexB, acmeC);
        savedOrders();

        List<OrdersModel> orders = cartService.checkout(EMAIL);

        assertThat(orders).hasSize(2);
        for (OrdersModel order : orders) {
            // Her siparişteki tüm kalemler tek bir şirkete ait
            assertThat(order.getItems()).extracting(i -> i.getProduct().getCompany().getCompanyId())
                    .containsOnly(order.getItems().get(0).getProduct().getCompany().getCompanyId());
        }
        assertThat(orders).extracting(o -> o.getItems().size()).containsExactlyInAnyOrder(2, 1);
    }

    @Test
    void checkoutLocksProductsInIdOrderToAvoidDeadlocks() {
        ProductsModel p30 = product(30, acme, 5, "1.00");
        ProductsModel p10 = product(10, acme, 5, "1.00");
        ProductsModel p20 = product(20, globex, 5, "1.00");
        when(cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(EMAIL)).thenReturn(List.of(
                cartItem(1, customer, p30, 1),
                cartItem(2, customer, p10, 1),
                cartItem(3, customer, p20, 1)));
        lockable(p10, p20, p30);
        savedOrders();

        cartService.checkout(EMAIL);

        InOrder lockOrder = inOrder(productRepository);
        lockOrder.verify(productRepository).findByIdForUpdate(10L);
        lockOrder.verify(productRepository).findByIdForUpdate(20L);
        lockOrder.verify(productRepository).findByIdForUpdate(30L);
    }

    @Test
    void checkoutFailsWhenStockIsNotEnough() {
        ProductsModel phone = product(10, acme, 1, "500.00");
        when(cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(EMAIL))
                .thenReturn(List.of(cartItem(1, customer, phone, 3)));
        lockable(phone);

        assertThatThrownBy(() -> cartService.checkout(EMAIL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock");
        verify(orderRepository, never()).saveAll(any());
        verify(cartRepository, never()).deleteAll(any());
    }

    @Test
    void checkoutWithEmptyCartFails() {
        when(cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(EMAIL)).thenReturn(List.of());

        assertThatThrownBy(() -> cartService.checkout(EMAIL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Your cart is empty.");
    }

    @Test
    void addToCartCannotExceedStock() {
        ProductsModel mouse = product(10, acme, 2, "50.00");
        when(userRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(mouse));
        when(cartRepository.findByCustomerAndProduct(customer, mouse))
                .thenReturn(Optional.of(cartItem(1, customer, mouse, 2)));

        CartItemData data = new CartItemData();
        data.setProductId(10L);
        data.setQuantity(1);

        assertThatThrownBy(() -> cartService.addToCart(EMAIL, data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Not enough stock.");
    }

    @Test
    void customerCannotChangeAnotherCustomersCartItem() {
        CartItemModel othersItem = cartItem(1, customer("other@test.com"), product(10, acme, 5, "1.00"), 1);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(othersItem));

        assertThatThrownBy(() -> cartService.removeItem(EMAIL, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart item not found.");
        verify(cartRepository, never()).delete(any());
    }
}
