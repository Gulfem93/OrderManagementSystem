package com.ordermanagementsystem.dev.data;

import java.math.BigDecimal;
import java.util.List;

import com.ordermanagementsystem.dev.data.ReportData.Bar;

// Owner > Reports: sipariş istatistikleri.
// Sipariş sayılarına tüm siparişler, tutarlara iptal/iade hariç siparişler girer.
public record OrderStats(
        List<Period> periods,       // Bugün, Son 7 gün, Son 30 gün (önceki dönemle karşılaştırmalı)
        List<Bar> dailyBars,        // son 14 günün sipariş sayısı
        List<Bar> weekdayBars,      // haftanın günlerine göre
        String busiestDay,
        String busiestHour,         // ör. "14:00 - 15:00"
        double avgItemsPerOrder,
        BigDecimal largestOrder,
        Long largestOrderId,
        int repeatCustomerRate,     // birden fazla sipariş veren müşterilerin yüzdesi
        long newCustomersThisMonth) {

    // change: önceki aynı uzunluktaki döneme göre % değişim (null = önceki dönemde sipariş yok)
    public record Period(String label, long orders, BigDecimal revenue, Integer change) {
        public boolean isUp() {
            return change != null && change > 0;
        }

        public boolean isDown() {
            return change != null && change < 0;
        }
    }
}
