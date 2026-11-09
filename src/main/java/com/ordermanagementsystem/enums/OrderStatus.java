package com.ordermanagementsystem.enums;

import java.util.List;

public enum OrderStatus {
    /*
     * Değer ----- Rozette yazan - Renk ------ Anlamı
     * PENDING --- Beklemede ----- Turuncu --- Sipariş alındı, owner henüz işlem
     * yapmadı
     * CONFIRMED - Onaylandı ----- Turkuaz --- Owner siparişi kabul etti
     * PREPARING - Hazırlanıyor -- Mavi ------ Ürünler paketleniyor
     * SHIPPED --- Kargoda ------- Mor ------- Kargoya verildi
     * DELIVERED - Teslim edildi - Yeşil ----- Müşteriye ulaştı
     * CANCELLED - İptal edildi -- Kırmızı --- Sipariş iptal oldu
     * RETURNED -- İade edildi --- Gri ------- Ürün geri gönderildi
     */
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    SHIPPED("Shipping"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    // Rozette yazan metin
    public String getLabel() {
        return label;
    }

    // Rozet rengi için CSS sınıfı: badge_pending, badge_cancelled ...
    public String getCssClass() {
        return "badge_" + name().toLowerCase();
    }

    // Customer sadece bekleyen siparişini iptal edebilir
    public boolean isCancellable() {
        return this == PENDING;
    }

    // Customer'ın My Orders sayfasındaki takip adımları (sırası getStep() ile aynı)
    public static final List<String> TRACK_STEPS = List.of("Order Placed", "Preparing", "Shipping", "Delivered");

    // Takip çubuğundaki aktif adım; iptal/iade için -1 (çubuk yerine rozet gösterilir)
    public int getStep() {
        return switch (this) {
            case PENDING -> 0;
            case CONFIRMED, PREPARING -> 1;
            case SHIPPED -> 2;
            case DELIVERED -> 4; // tüm adımlar tamamlandı

            case CANCELLED, RETURNED -> -1;
        };
    }

    public boolean isTrackable() {
        return getStep() >= 0;
    }

    // Customer'ın gördüğü metin: owner onayladıysa "Preparing"
    public String getCustomerLabel() {
        return switch (this) {
            case PENDING -> "Awaiting Approval";
            case CONFIRMED, PREPARING -> "Preparing";
            default -> label;
        };
    }

    public String getCustomerCssClass() {
        return this == CONFIRMED ? PREPARING.getCssClass() : getCssClass();
    }

    // Owner'ın durum seçim kutusunda yazan eylem
    public String getActionLabel() {
        return switch (this) {
            case CONFIRMED -> "Confirm";
            case PREPARING -> "Approve → Preparing";
            case SHIPPED -> "Ship → Shipping";
            case DELIVERED -> "Mark Delivered";
            case CANCELLED -> "Cancel Order";
            case RETURNED -> "Mark Returned";
            case PENDING -> label;
        };
    }

    // Kargodaki siparişi customer "Teslim aldım" diyerek Delivered yapabilir
    public boolean isReceivable() {
        return this == SHIPPED;
    }

    // Owner'ın bu durumdan geçebileceği durumlar (Orders sayfasındaki seçenekler).
    // Onay doğrudan PREPARING'e geçirir; CONFIRMED sadece eski kayıtlar için kaldı.
    public List<OrderStatus> getNextStatuses() {
        return switch (this) {
            case PENDING -> List.of(PREPARING, CANCELLED);
            case CONFIRMED -> List.of(PREPARING, CANCELLED);
            case PREPARING -> List.of(SHIPPED, CANCELLED);
            case SHIPPED -> List.of(DELIVERED);
            case DELIVERED -> List.of(RETURNED);
            case CANCELLED, RETURNED -> List.of();
        };
    }

    public boolean canChangeTo(OrderStatus next) {
        return getNextStatuses().contains(next);
    }

    // İptal ve iadede ürünler stoğa geri döner
    public boolean restoresStock() {
        return this == CANCELLED || this == RETURNED;
    }
}
