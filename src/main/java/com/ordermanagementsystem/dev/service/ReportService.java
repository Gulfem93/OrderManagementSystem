package com.ordermanagementsystem.dev.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ordermanagementsystem.dev.data.OrderStats;
import com.ordermanagementsystem.dev.data.OrderStats.Period;
import com.ordermanagementsystem.dev.data.OrderView;
import com.ordermanagementsystem.dev.data.OwnerInsights;
import com.ordermanagementsystem.dev.model.ProductsModel;
import com.ordermanagementsystem.dev.data.ReportData;
import com.ordermanagementsystem.dev.data.ReportData.Bar;
import com.ordermanagementsystem.dev.data.ReportData.ProductRow;
import com.ordermanagementsystem.dev.model.OrderItemModel;
import com.ordermanagementsystem.dev.model.OrdersModel;
import com.ordermanagementsystem.enums.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final int MONTHS = 6;
    private static final int TOP_PRODUCTS = 5;
    private static final int TOP_BREAKDOWN = 5;
    private static final int DAILY_DAYS = 14;
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM");

    private final OrderService orderService;
    private final ProductService productService;

    // Customer: kendi siparişleri, harcamanın şirketlere dağılımı
    @Transactional(readOnly = true)
    public ReportData customerReport(String customerEmail) {
        List<OrderView> views = orderService.getOrderViewsForCustomer(customerEmail);
        return build(views, false, "Spending by Company",
                v -> v.items().stream().map(i -> new Share(companyOf(i), i.getLineTotal())).toList());
    }

    // Owner: şirketinin satışları (sadece kendi kalemleri), en çok alan müşteriler
    @Transactional(readOnly = true)
    public ReportData ownerReport(String company) {
        List<OrderView> views = orderService.getOrderViewsForCompany(company, null);
        return build(views, true, "Top Customers",
                v -> List.of(new Share(customerNameOf(v.order()), v.total())));
    }

    // Owner: satış sağlığı (onay bekleyen, iptal/iade oranı, kategoriler) + stok raporu
    @Transactional(readOnly = true)
    public OwnerInsights ownerInsights(String company, long lowStockThreshold) {
        List<OrderView> views = orderService.getOrderViewsForCompany(company, null);
        List<ProductsModel> products = productService.findByCompany(company);

        long awaiting = views.stream().filter(v -> v.order().getOrderStatus() == OrderStatus.PENDING).count();
        List<OrderView> open = views.stream().filter(v -> isOpen(v.order().getOrderStatus())).toList();
        BigDecimal openRevenue = sum(views.stream()
                .filter(v -> v.order().getOrderStatus() == OrderStatus.PENDING || isOpen(v.order().getOrderStatus()))
                .toList());
        BigDecimal delivered = sum(views.stream()
                .filter(v -> v.order().getOrderStatus() == OrderStatus.DELIVERED).toList());
        long cancelled = views.stream().filter(v -> v.order().getOrderStatus() == OrderStatus.CANCELLED).count();
        long returned = views.stream().filter(v -> v.order().getOrderStatus() == OrderStatus.RETURNED).count();

        List<OrderView> active = views.stream().filter(v -> !restoresStock(v.order())).toList();
        List<Bar> categoryBars = amountBars(active.stream()
                .flatMap(v -> v.items().stream())
                .map(i -> new Share(categoryOf(i.getProduct()), i.getLineTotal()))
                .toList(), 6);

        // Stok
        long totalUnits = products.stream().mapToLong(p -> stockOf(p)).sum();
        BigDecimal stockValue = products.stream()
                .filter(p -> p.getProductPrice() != null)
                .map(p -> p.getProductPrice().multiply(BigDecimal.valueOf(stockOf(p))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long outOfStock = products.stream().filter(p -> stockOf(p) == 0).count();
        List<ProductsModel> lowStock = products.stream()
                .filter(p -> stockOf(p) <= lowStockThreshold)
                .sorted(Comparator.comparingLong(ReportService::stockOf))
                .toList();

        // Hiç satılmamış ürünler (iptal/iade edilenler satış sayılmaz)
        Set<Long> soldIds = new HashSet<>();
        active.forEach(v -> v.items().forEach(i -> {
            if (i.getProduct() != null) {
                soldIds.add(i.getProduct().getProductId());
            }
        }));
        List<ProductsModel> unsold = products.stream()
                .filter(p -> !soldIds.contains(p.getProductId()))
                .toList();

        return new OwnerInsights(awaiting, open.size(), openRevenue, delivered,
                percent(cancelled, views.size()), percent(returned, views.size()), categoryBars,
                products.size(), totalUnits, stockValue, outOfStock, lowStockThreshold, lowStock, unsold);
    }

    // Owner: sipariş istatistikleri (dönem karşılaştırması, günlük grafik, yoğun gün/saat...)
    @Transactional(readOnly = true)
    public OrderStats orderStats(String company) {
        List<OrderView> views = orderService.getOrderViewsForCompany(company, null).stream()
                .filter(v -> v.order().getOrderDate() != null)
                .toList();
        LocalDate today = LocalDate.now();

        List<Period> periods = List.of(
                period("Today", views, today, 1),
                period("Last 7 Days", views, today, 7),
                period("Last 30 Days", views, today, 30));

        // Son 14 gün
        Map<LocalDate, Long> perDay = new LinkedHashMap<>();
        for (int i = DAILY_DAYS - 1; i >= 0; i--) {
            perDay.put(today.minusDays(i), 0L);
        }
        views.forEach(v -> perDay.computeIfPresent(v.order().getOrderDate().toLocalDate(), (d, c) -> c + 1));
        long maxDay = perDay.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<Bar> dailyBars = new ArrayList<>();
        perDay.forEach((d, c) -> dailyBars.add(new Bar(d.format(DAY_FORMAT), null, c, null, percent(c, maxDay))));

        // Haftanın günleri
        Map<DayOfWeek, Long> perWeekday = new LinkedHashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            perWeekday.put(d, 0L);
        }
        views.forEach(v -> perWeekday.merge(v.order().getOrderDate().getDayOfWeek(), 1L, Long::sum));
        long maxWeekday = perWeekday.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<Bar> weekdayBars = new ArrayList<>();
        perWeekday.forEach((d, c) -> weekdayBars.add(new Bar(
                d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), null, c, null, percent(c, maxWeekday))));
        String busiestDay = views.isEmpty() ? "-"
                : perWeekday.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow()
                        .getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // En yoğun saat
        Map<Integer, Long> perHour = new HashMap<>();
        views.forEach(v -> perHour.merge(v.order().getOrderDate().getHour(), 1L, Long::sum));
        String busiestHour = perHour.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00 - %02d:00", e.getKey(), (e.getKey() + 1) % 24))
                .orElse("-");

        // Sipariş başına ürün, en büyük sipariş (iptal/iade hariç)
        List<OrderView> active = views.stream().filter(v -> !restoresStock(v.order())).toList();
        double avgItems = active.stream()
                .mapToLong(v -> v.items().stream()
                        .mapToLong(i -> i.getOrderitemQuantity() == null ? 0 : i.getOrderitemQuantity()).sum())
                .average().orElse(0);
        OrderView largest = active.stream().max(Comparator.comparing(OrderView::total)).orElse(null);

        // Tekrar eden müşteriler ve bu ay ilk siparişini veren müşteriler
        Map<Long, List<OrderView>> byCustomer = new HashMap<>();
        views.stream().filter(v -> v.order().getCustomer() != null)
                .forEach(v -> byCustomer.computeIfAbsent(v.order().getCustomer().getCustomerId(),
                        k -> new ArrayList<>()).add(v));
        long repeat = byCustomer.values().stream().filter(l -> l.size() > 1).count();
        YearMonth thisMonth = YearMonth.now();
        long newThisMonth = byCustomer.values().stream()
                .map(l -> l.stream().map(v -> v.order().getOrderDate()).min(Comparator.naturalOrder()).orElseThrow())
                .filter(first -> YearMonth.from(first).equals(thisMonth))
                .count();

        return new OrderStats(periods, dailyBars, weekdayBars, busiestDay, busiestHour,
                Math.round(avgItems * 10) / 10.0,
                largest == null ? BigDecimal.ZERO : largest.total(),
                largest == null ? null : largest.order().getOrderId(),
                percent(repeat, byCustomer.size()), newThisMonth);
    }

    // Son 'days' günün siparişleri ve önceki aynı uzunluktaki dönemle karşılaştırma
    private Period period(String label, List<OrderView> views, LocalDate today, int days) {
        LocalDate start = today.minusDays(days - 1);
        LocalDate prevStart = start.minusDays(days);
        List<OrderView> current = between(views, start, today);
        long previous = between(views, prevStart, start.minusDays(1)).size();
        BigDecimal revenue = sum(current.stream().filter(v -> !restoresStock(v.order())).toList());
        Integer change = previous == 0 ? null
                : (int) Math.round((current.size() - previous) * 100.0 / previous);
        return new Period(label, current.size(), revenue, change);
    }

    private static List<OrderView> between(List<OrderView> views, LocalDate from, LocalDate to) {
        return views.stream().filter(v -> {
            LocalDate d = v.order().getOrderDate().toLocalDate();
            return !d.isBefore(from) && !d.isAfter(to);
        }).toList();
    }

    // Onaylanmış ama henüz teslim edilmemiş
    private static boolean isOpen(OrderStatus s) {
        return s == OrderStatus.CONFIRMED || s == OrderStatus.PREPARING || s == OrderStatus.SHIPPED;
    }

    private static BigDecimal sum(List<OrderView> views) {
        return views.stream().map(OrderView::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long stockOf(ProductsModel p) {
        return p.getProductStock() == null ? 0 : p.getProductStock();
    }

    private static String categoryOf(ProductsModel p) {
        // Satışlar ana kategoriye göre toplanır (Laptop, Tablet -> Elektronik)
        return p == null || p.getProductCategory() == null
                ? "Uncategorized"
                : p.getProductCategory().getRoot().getDisplayName();
    }

    private record Share(String key, BigDecimal amount) {
    }

    private ReportData build(List<OrderView> views, boolean owner, String breakdownTitle,
            Function<OrderView, List<Share>> breakdown) {
        // İptal/iade edilenler tutar hesabına girmez
        List<OrderView> active = views.stream().filter(v -> !restoresStock(v.order())).toList();

        BigDecimal total = active.stream().map(OrderView::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = active.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
        long itemCount = active.stream()
                .flatMap(v -> v.items().stream())
                .mapToLong(i -> i.getOrderitemQuantity() == null ? 0 : i.getOrderitemQuantity())
                .sum();

        List<Bar> breakdownBars = amountBars(active.stream()
                .flatMap(v -> breakdown.apply(v).stream())
                .toList(), TOP_BREAKDOWN);

        return new ReportData(views.size(), active.size(), total, average, itemCount,
                statusBars(views, owner), monthlyBars(active), topProducts(active),
                breakdownTitle, breakdownBars);
    }

    // Duruma göre sipariş sayısı. Customer CONFIRMED'i de "Preparing" olarak görür.
    private List<Bar> statusBars(List<OrderView> views, boolean owner) {
        Map<String, long[]> counts = new LinkedHashMap<>();
        Map<String, String> css = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            String label = owner ? s.getLabel() : s.getCustomerLabel();
            css.putIfAbsent(label, owner ? s.getCssClass() : s.getCustomerCssClass());
            long count = views.stream().filter(v -> v.order().getOrderStatus() == s).count();
            counts.computeIfAbsent(label, k -> new long[1])[0] += count;
        }
        List<Bar> bars = new ArrayList<>();
        counts.forEach((label, c) -> {
            if (c[0] > 0) {
                bars.add(new Bar(label, css.get(label), c[0], null, percent(c[0], views.size())));
            }
        });
        return bars;
    }

    // Son 6 ayın tutarı (bu ay dahil)
    private List<Bar> monthlyBars(List<OrderView> active) {
        YearMonth now = YearMonth.now();
        Map<YearMonth, BigDecimal> sums = new LinkedHashMap<>();
        Map<YearMonth, Long> counts = new LinkedHashMap<>();
        for (int i = MONTHS - 1; i >= 0; i--) {
            sums.put(now.minusMonths(i), BigDecimal.ZERO);
            counts.put(now.minusMonths(i), 0L);
        }
        for (OrderView v : active) {
            if (v.order().getOrderDate() == null) {
                continue;
            }
            YearMonth ym = YearMonth.from(v.order().getOrderDate());
            if (sums.containsKey(ym)) {
                sums.merge(ym, v.total(), BigDecimal::add);
                counts.merge(ym, 1L, Long::sum);
            }
        }
        BigDecimal max = sums.values().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        List<Bar> bars = new ArrayList<>();
        sums.forEach((ym, sum) -> bars.add(new Bar(
                ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear(),
                null, counts.get(ym), sum, percent(sum, max))));
        return bars;
    }

    // En çok tutarı olan ürünler
    private List<ProductRow> topProducts(List<OrderView> active) {
        Map<Long, ProductRow> rows = new LinkedHashMap<>();
        for (OrderView v : active) {
            for (OrderItemModel i : v.items()) {
                if (i.getProduct() == null) {
                    continue;
                }
                long qty = i.getOrderitemQuantity() == null ? 0 : i.getOrderitemQuantity();
                rows.merge(i.getProduct().getProductId(),
                        new ProductRow(i.getProduct().getProductName(), companyOf(i), qty, i.getLineTotal()),
                        (a, b) -> new ProductRow(a.name(), a.company(), a.quantity() + b.quantity(),
                                a.amount().add(b.amount())));
            }
        }
        return rows.values().stream()
                .sorted(Comparator.comparing(ProductRow::amount).reversed())
                .limit(TOP_PRODUCTS)
                .toList();
    }

    // Anahtara göre toplanan tutarlar, büyükten küçüğe
    private List<Bar> amountBars(List<Share> shares, int limit) {
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Share s : shares) {
            sums.merge(s.key(), s.amount(), BigDecimal::add);
            counts.merge(s.key(), 1L, Long::sum);
        }
        BigDecimal max = sums.values().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        return sums.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new Bar(e.getKey(), null, counts.get(e.getKey()), e.getValue(), percent(e.getValue(), max)))
                .toList();
    }

    private static boolean restoresStock(OrdersModel order) {
        return order.getOrderStatus() != null && order.getOrderStatus().restoresStock();
    }

    private static String companyOf(OrderItemModel i) {
        String c = i.getProduct() == null ? null : i.getProduct().getCompanyName();
        return c == null || c.isBlank() ? "-" : c;
    }

    private static String customerNameOf(OrdersModel order) {
        if (order.getCustomer() == null) {
            return "-";
        }
        String surname = order.getCustomer().getCustomerSurname();
        return (order.getCustomer().getCustomerName() + " " + (surname == null ? "" : surname)).trim();
    }

    private static int percent(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    private static int percent(BigDecimal part, BigDecimal max) {
        if (max.signum() == 0) {
            return 0;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
    }
}
