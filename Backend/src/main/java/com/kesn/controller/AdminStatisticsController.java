package com.kesn.controller;

import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;
import com.kesn.entity.Product;
import com.kesn.repository.OrderRepository;
import com.kesn.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/statistics")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174"})
public class AdminStatisticsController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminStatisticsController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> get(
            @RequestParam(defaultValue = "day") String range,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        ZoneId z = ZoneId.systemDefault();
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(z);
        List<Order> all = orderRepository.findAll();

        Map<String, Object> periodCards = new LinkedHashMap<>();
        periodCards.put("today", periodMetrics(all, startOfDay(today, z), now, true));
        periodCards.put("week", periodMetrics(all, startOfWeek(today, z), now, true));
        periodCards.put("month", periodMetrics(all, startOfMonth(today, z), now, true));
        periodCards.put("year", periodMetrics(all, startOfYear(today, z), now, true));

        TimeRange fr = resolveFilterRange(range, from, to, z, now);
        List<Order> filtered = filterInclusiveEnd(all, fr.start(), fr.end());

        Map<String, Object> out = new HashMap<>();
        out.put("periodCards", periodCards);
        out.put("filterRange", Map.of(
                "preset", range,
                "start", fr.start().toString(),
                "end", fr.end().toString()
        ));
        out.put("bestSelling", bestSelling(filtered, productRepository.findAll(), 15));
        out.put("orderStatusPie", orderStatusPie(filtered));
        out.put("growthPanel", growthPanel(all, z, today, now));
        out.put("lowStock", Collections.emptyList());
        return ResponseEntity.ok(out);
    }

    private record TimeRange(Instant start, Instant end) {}

    private static Instant startOfDay(LocalDate d, ZoneId z) {
        return d.atStartOfDay(z).toInstant();
    }

    private static Instant startOfWeek(LocalDate today, ZoneId z) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(z).toInstant();
    }

    private static Instant startOfMonth(LocalDate today, ZoneId z) {
        return today.withDayOfMonth(1).atStartOfDay(z).toInstant();
    }

    private static Instant startOfYear(LocalDate today, ZoneId z) {
        return today.withDayOfYear(1).atStartOfDay(z).toInstant();
    }

    private static TimeRange resolveFilterRange(String range, String fromIso, String toIso, ZoneId z, Instant now) {
        try {
            if ("custom".equalsIgnoreCase(range) && fromIso != null && toIso != null
                    && !fromIso.isBlank() && !toIso.isBlank()) {
                Instant fs = Instant.parse(fromIso.trim());
                Instant te = Instant.parse(toIso.trim());
                if (fs.isBefore(te)) return new TimeRange(fs, te);
            }
        } catch (Exception ignored) { }
        LocalDate today = LocalDate.now(z);
        String r = range == null ? "day" : range.toLowerCase(Locale.ROOT);
        if ("week".equals(r)) {
            return new TimeRange(startOfWeek(today, z), now);
        }
        if ("month".equals(r)) {
            return new TimeRange(startOfMonth(today, z), now);
        }
        if ("year".equals(r)) {
            return new TimeRange(startOfYear(today, z), now);
        }
        return new TimeRange(startOfDay(today, z), now);
    }

    /** inclusive end (dùng cho đến "bây giờ") */
    private static List<Order> filterInclusiveEnd(List<Order> all, Instant start, Instant end) {
        return all.stream().filter(o -> {
            if (o.getCreatedAt() == null) return false;
            Instant t = o.getCreatedAt();
            return !t.isBefore(start) && !t.isAfter(end);
        }).collect(Collectors.toList());
    }

    /** [start, endExclusive) */
    private static List<Order> filterHalfOpen(List<Order> all, Instant start, Instant endExclusive) {
        return all.stream().filter(o -> {
            if (o.getCreatedAt() == null) return false;
            Instant t = o.getCreatedAt();
            return !t.isBefore(start) && t.isBefore(endExclusive);
        }).collect(Collectors.toList());
    }

    private static Map<String, Object> periodMetrics(List<Order> all, Instant start, Instant end, boolean inclusiveEnd) {
        List<Order> os = inclusiveEnd ? filterInclusiveEnd(all, start, end) : filterHalfOpen(all, start, end);
        BigDecimal revenue = sumTotals(os);
        int itemsSold = sumItemQty(os);
        long ok = os.stream().filter(o -> "delivered".equalsIgnoreCase(o.getStatus())).count();
        long cancelled = os.stream().filter(o -> "cancelled".equalsIgnoreCase(o.getStatus())).count();
        long returned = os.stream().filter(o -> "returned".equalsIgnoreCase(o.getStatus())).count();
        Map<String, Object> m = new HashMap<>();
        m.put("revenue", revenue);
        m.put("itemsSold", itemsSold);
        m.put("ordersSuccess", ok);
        m.put("ordersCancelled", cancelled);
        m.put("ordersReturned", returned);
        return m;
    }

    private static BigDecimal sumTotals(List<Order> os) {
        return os.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int sumItemQty(List<Order> os) {
        int n = 0;
        for (Order o : os) {
            if (o.getItems() == null) continue;
            for (OrderItem it : o.getItems()) {
                n += it.getQuantity() != null ? it.getQuantity() : 0;
            }
        }
        return n;
    }

    private static class NameAgg {
        int qty;
        BigDecimal unitPrice = BigDecimal.ZERO;
    }

    private static List<Map<String, Object>> bestSelling(List<Order> filtered, List<Product> products, int limit) {
        Map<String, NameAgg> map = new HashMap<>();
        for (Order o : filtered) {
            if (o.getItems() == null) continue;
            for (OrderItem it : o.getItems()) {
                String name = it.getProductName() == null ? "—" : it.getProductName().trim();
                NameAgg a = map.computeIfAbsent(name, k -> new NameAgg());
                a.qty += it.getQuantity() != null ? it.getQuantity() : 0;
                if (it.getUnitPrice() != null) a.unitPrice = it.getUnitPrice();
            }
        }
        Map<String, String> imgByName = new HashMap<>();
        for (Product p : products) {
            if (p.getName() != null) {
                imgByName.put(p.getName().trim().toLowerCase(), p.getImageUrl());
            }
        }
        return map.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().qty, a.getValue().qty))
                .limit(limit)
                .map(e -> {
                    String name = e.getKey();
                    NameAgg a = e.getValue();
                    String img = imgByName.get(name.toLowerCase());
                    Map<String, Object> row = new HashMap<>();
                    row.put("productName", name);
                    row.put("quantity", a.qty);
                    row.put("unitPrice", a.unitPrice);
                    row.put("imageUrl", img);
                    row.put("sizes", "—");
                    return row;
                })
                .collect(Collectors.toList());
    }

    private static List<Map<String, Object>> orderStatusPie(List<Order> orders) {
        Map<String, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus().toLowerCase(Locale.ROOT) : "unknown",
                        Collectors.counting()));
        long total = orders.size();
        List<Map<String, Object>> slices = new ArrayList<>();
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        labels.put("pending", "Chờ xử lý");
        labels.put("shipping", "Đang giao hàng");
        labels.put("delivered", "Đã giao hàng");
        labels.put("cancelled", "Đã hủy");
        labels.put("returned", "Trả hàng");
        if (total == 0) {
            for (Map.Entry<String, String> en : labels.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("key", en.getKey());
                row.put("label", en.getValue());
                row.put("count", 0L);
                row.put("percent", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                slices.add(row);
            }
            return slices;
        }
        for (Map.Entry<String, String> en : labels.entrySet()) {
            long c = counts.getOrDefault(en.getKey(), 0L);
            double pct = (100.0 * c) / total;
            Map<String, Object> row = new HashMap<>();
            row.put("key", en.getKey());
            row.put("label", en.getValue());
            row.put("count", c);
            row.put("percent", BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP));
            slices.add(row);
        }
        for (Map.Entry<String, Long> e : counts.entrySet()) {
            if (labels.containsKey(e.getKey())) continue;
            long c = e.getValue();
            double pct = (100.0 * c) / total;
            Map<String, Object> row = new HashMap<>();
            row.put("key", e.getKey());
            row.put("label", e.getKey());
            row.put("count", c);
            row.put("percent", BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP));
            slices.add(row);
        }
        return slices;
    }

    private static List<Map<String, Object>> growthPanel(List<Order> all, ZoneId z, LocalDate today, Instant now) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Instant startToday = startOfDay(today, z);
        Instant startYesterday = startOfDay(today.minusDays(1), z);

        rows.add(growthMoney("Doanh thu ngày",
                sumTotals(filterInclusiveEnd(all, startToday, now)),
                sumTotals(filterHalfOpen(all, startYesterday, startToday))));

        Instant wkStart = startOfWeek(today, z);
        long wkSec = Math.max(ChronoUnit.SECONDS.between(wkStart, now), 1L);
        Instant prevWkEnd = wkStart.minusMillis(1);
        Instant prevWkStart = wkStart.minusSeconds(wkSec);
        rows.add(growthMoney("Doanh thu tuần",
                sumTotals(filterInclusiveEnd(all, wkStart, now)),
                sumTotals(filterInclusiveEnd(all, prevWkStart, prevWkEnd))));

        Instant moStart = startOfMonth(today, z);
        long moSec = Math.max(ChronoUnit.SECONDS.between(moStart, now), 1L);
        Instant prevMoEnd = moStart.minusMillis(1);
        Instant prevMoStart = moStart.minusSeconds(moSec);
        rows.add(growthMoney("Doanh thu tháng",
                sumTotals(filterInclusiveEnd(all, moStart, now)),
                sumTotals(filterInclusiveEnd(all, prevMoStart, prevMoEnd))));

        Instant yrStart = startOfYear(today, z);
        long yrSec = Math.max(ChronoUnit.SECONDS.between(yrStart, now), 1L);
        Instant prevYrEnd = yrStart.minusMillis(1);
        Instant prevYrStart = yrStart.minusSeconds(yrSec);
        rows.add(growthMoney("Doanh thu năm",
                sumTotals(filterInclusiveEnd(all, yrStart, now)),
                sumTotals(filterInclusiveEnd(all, prevYrStart, prevYrEnd))));

        rows.add(growthCount("Đơn hàng ngày",
                filterInclusiveEnd(all, startToday, now).size(),
                filterHalfOpen(all, startYesterday, startToday).size()));
        rows.add(growthCount("Đơn hàng tuần",
                filterInclusiveEnd(all, wkStart, now).size(),
                filterInclusiveEnd(all, prevWkStart, prevWkEnd).size()));
        rows.add(growthCount("Đơn hàng tháng",
                filterInclusiveEnd(all, moStart, now).size(),
                filterInclusiveEnd(all, prevMoStart, prevMoEnd).size()));
        rows.add(growthCount("Đơn hàng năm",
                filterInclusiveEnd(all, yrStart, now).size(),
                filterInclusiveEnd(all, prevYrStart, prevYrEnd).size()));
        rows.add(growthCount("Sản phẩm (SL bán) ngày",
                sumItemQty(filterInclusiveEnd(all, startToday, now)),
                sumItemQty(filterHalfOpen(all, startYesterday, startToday))));
        return rows;
    }

    private static Map<String, Object> growthMoney(String label, BigDecimal cur, BigDecimal prev) {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("value", cur);
        m.put("trendPct", pctChange(cur, prev));
        m.put("type", "money");
        return m;
    }

    private static Map<String, Object> growthCount(String label, int cur, int prev) {
        Map<String, Object> m = new HashMap<>();
        m.put("label", label);
        m.put("value", cur);
        m.put("trendPct", prev == 0 ? (cur > 0 ? 100.0 : 0.0) : (100.0 * (cur - prev) / prev));
        m.put("type", "count");
        return m;
    }

    private static double pctChange(BigDecimal cur, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            return cur.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return cur.subtract(prev).multiply(BigDecimal.valueOf(100))
                .divide(prev, 1, RoundingMode.HALF_UP).doubleValue();
    }
}
