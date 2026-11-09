package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.util.List;

import com.ordermanagementsystem.dev.data.ReportData.Bar;
import com.ordermanagementsystem.dev.model.ProductsModel;

// Owner > Reports: satış sağlığı ve stok raporu (ReportData'nın owner'a özel eki)
public record OwnerInsights(
        // Satış
        long awaitingApproval,      // PENDING: owner'ın onayını bekleyen
        long openOrderCount,        // onaylanmış, henüz teslim edilmemiş
        BigDecimal openRevenue,     // bekleyen + yoldaki siparişlerin tutarı
        BigDecimal deliveredRevenue,// teslim edilmiş (kesinleşmiş) satış
        int cancelRate,             // iptal edilen siparişlerin yüzdesi
        int returnRate,             // iade edilen siparişlerin yüzdesi
        List<Bar> categoryBars,     // kategoriye göre satış

        // Stok
        long productCount,
        long totalUnits,
        BigDecimal stockValue,      // fiyat x stok
        long outOfStockCount,
        long lowStockThreshold,
        List<ProductsModel> lowStockProducts,
        List<ProductsModel> unsoldProducts) {
}
