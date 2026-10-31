package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.CartItemData;
import com.ordermanagementsystem.dev.model.CartItemModel;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.model.UsersModel;
import com.ordermanagementsystem.dev.repository.CartRepository;
import com.ordermanagementsystem.dev.repository.ProductRepository;
import com.ordermanagementsystem.dev.repository.UserRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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