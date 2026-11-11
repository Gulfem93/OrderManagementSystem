package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.CartItemData;
import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.model.OrderItemModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CartRepository;
import com.ordermanagementsystem.dev.repository.OrderRepository;
import com.ordermanagementsystem.dev.repository.ProductRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;
import com.ordermanagementsystem.enums.OrderStatus;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CartService(CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public CartItemModel addToCart(String userEmail, CartItemData data) {
        int quantity = (data.getQuantity() == null || data.getQuantity() < 1) ? 1 : data.getQuantity();

        UsersModel customer = userRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + userEmail));

        ProductsModel product = productRepository.findById(data.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı: " + data.getProductId()));

        // Sepette bu ürün varsa adedini artır, yoksa yeni kalem oluştur
        CartItemModel item = cartRepository.findByCustomerAndProduct(customer, product)
                .orElseGet(() -> {
                    CartItemModel newItem = new CartItemModel();
                    newItem.setCustomer(customer);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + quantity;

        // Sepetteki toplam adet stoğu geçmesin
        Long stock = product.getProductStock();
        if (stock == null || stock < newQuantity) {
            throw new IllegalStateException("Yeterli stok yok");
        }

        item.setQuantity(newQuantity);
        return cartRepository.save(item);
    }

    // Sepetteki kalemin adedini delta kadar değiştirir (+1 / -1); adet 0'a inerse kalem silinir
    @Transactional
    public void changeQuantity(String userEmail, Long cartItemId, int delta) {
        CartItemModel item = findOwnItem(userEmail, cartItemId);
        int newQuantity = item.getQuantity() + delta;

        if (newQuantity < 1) {
            cartRepository.delete(item);
            return;
        }

        Long stock = item.getProduct().getProductStock();
        if (stock == null || stock < newQuantity) {
            throw new IllegalStateException("Yeterli stok yok");
        }

        item.setQuantity(newQuantity);
        cartRepository.save(item);
    }

    @Transactional
    public void removeItem(String userEmail, Long cartItemId) {
        cartRepository.delete(findOwnItem(userEmail, cartItemId));
    }

    // Kullanıcı sadece kendi sepetindeki kalemi değiştirebilir
    private CartItemModel findOwnItem(String userEmail, Long cartItemId) {
        CartItemModel item = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Sepet kalemi bulunamadı: " + cartItemId));
        if (!item.getCustomer().getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Sepet kalemi bulunamadı: " + cartItemId);
        }
        return item;
    }

    // Sepeti siparişe çevirir: stokları düşer, sipariş oluşturur, sepeti boşaltır.
    // Herhangi bir üründe stok yetmezse hiçbir değişiklik kaydedilmez (transaction geri alınır).
    @Transactional
    public OrdersModel checkout(String userEmail) {
        List<CartItemModel> cartItems = cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(userEmail);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Sepetiniz boş");
        }

        OrdersModel order = new OrdersModel();
        // Sipariş doğrudan kullanıcıya bağlanır (ayrı customers kaydı yok)
        order.setCustomer(cartItems.get(0).getCustomer());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        for (CartItemModel cartItem : cartItems) {
            ProductsModel product = productRepository.findByIdForUpdate(cartItem.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Ürün bulunamadı"));

            long quantity = cartItem.getQuantity();
            Long stock = product.getProductStock();
            if (stock == null || stock < quantity) {
                throw new IllegalStateException("Yeterli stok yok: " + product.getProductName());
            }
            product.setProductStock(stock - quantity);

            OrderItemModel orderItem = new OrderItemModel();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setOrderitemQuantity(quantity);
            orderItem.setOrderitemUnitPrice(product.getProductPrice());
            order.getItems().add(orderItem);
        }

        OrdersModel saved = orderRepository.save(order);
        cartRepository.deleteAll(cartItems);
        return saved;
    }

    // Customer: sepetindeki ürünler (My Orders sayfası için), en son eklenen üstte
    public List<CartItemModel> getCartItems(String userEmail) {
        return cartRepository.findByCustomer_UserEmailOrderByCartItemIdDesc(userEmail);
    }

    public BigDecimal getCartTotal(List<CartItemModel> items) {
        return items.stream()
                .map(CartItemModel::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}