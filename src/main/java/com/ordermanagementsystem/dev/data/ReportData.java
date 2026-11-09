package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.util.List;

// Reports sayfası. Customer: satın alma raporu, Owner: satış raporu.
// Tutarlara iptal ve iade edilen siparişler dahil değildir.
public record ReportData(
        long orderCount,
        long activeOrderCount,
        BigDecimal totalAmount,
        BigDecimal averageOrder,
        long itemCount,
        List<Bar> statusBars,
        List<Bar> monthlyBars,
        List<ProductRow> topProducts,
        String breakdownTitle,
        List<Bar> breakdownBars) {

    // Çubuk grafikteki bir satır; percent çubuğun genişliği (0-100)
    public record Bar(String label, String cssClass, long count, BigDecimal amount, int percent) {
    }

    public record ProductRow(String name, String company, long quantity, BigDecimal amount) {
    }
}
