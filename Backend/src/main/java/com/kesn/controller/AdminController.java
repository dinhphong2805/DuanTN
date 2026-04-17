
package com.kesn.controller;

import com.kesn.dto.AdminStatsDto;
import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;
import com.kesn.entity.Product;
import com.kesn.repository.OrderRepository;
import com.kesn.repository.ProductRepository;
import com.kesn.repository.UserRepository;
import com.kesn.repository.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174"})
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminController(ProductRepository productRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. THỐNG KÊ & DOANH THU 
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> stats() {
        long products = productRepository.count();
        long orders = orderRepository.count();
        long users = userRepository.count();
        return ResponseEntity.ok(new AdminStatsDto(products, orders, users));
    }

    @GetMapping("/revenue-report")
    public ResponseEntity<Map<String, Object>> revenueReport(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        int topN = Math.max(1, Math.min(limit == null ? 10 : limit, 100));
        List<Order> orders = orderRepository.findAll();
        TimeRange reportRange = resolveReportRange(days, from, to);
        if (reportRange != null) {
            orders = filterOrdersInRange(orders, reportRange.start(), reportRange.end());
        }

        BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long deliveredOrders = orders.stream()
                .filter(o -> "delivered".equalsIgnoreCase(o.getStatus()))
                .count();
        BigDecimal deliveredRevenue = orders.stream()
                .filter(o -> "delivered".equalsIgnoreCase(o.getStatus()))
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, String> categoryByProductName = productRepository.findAll().stream()
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .collect(Collectors.toMap(
                        p -> p.getName().trim().toLowerCase(),
                        p -> (p.getCategory() == null || p.getCategory().isBlank()) ? "Khong xac dinh" : p.getCategory().trim(),
                        (a, b) -> a
                ));

        Map<String, RevenueAgg> byCategory = new HashMap<>();
        Map<String, RevenueAgg> byProduct = new HashMap<>();
        Map<String, CustomerAgg> byCustomer = new HashMap<>();

        for (Order order : orders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    String productName = (item.getProductName() == null || item.getProductName().isBlank())
                            ? "Khong xac dinh"
                            : item.getProductName().trim();
                    int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                    BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal lineRevenue = unitPrice.multiply(BigDecimal.valueOf(qty));

                    String category = categoryByProductName.getOrDefault(productName.trim().toLowerCase(), "Khong xac dinh");
                    byCategory.computeIfAbsent(category, k -> new RevenueAgg()).add(lineRevenue, qty);
                    byProduct.computeIfAbsent(productName, k -> new RevenueAgg()).add(lineRevenue, qty);
                }
            }

            String customerKey;
            if (order.getUserId() != null) {
                customerKey = "user:" + order.getUserId();
            } else if (order.getCustomerEmail() != null && !order.getCustomerEmail().isBlank()) {
                customerKey = "email:" + order.getCustomerEmail().trim().toLowerCase();
            } else {
                String name = order.getCustomerName() != null ? order.getCustomerName().trim().toLowerCase() : "guest";
                customerKey = "name:" + name;
            }
            byCustomer.computeIfAbsent(customerKey, k -> new CustomerAgg(order.getCustomerName(), order.getCustomerEmail()))
                    .add(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
        }

        List<Map<String, Object>> revenueByCategory = byCategory.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "category", e.getKey(),
                        "revenue", e.getValue().revenue,
                        "quantity", e.getValue().quantity
                ))
                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        List<Map<String, Object>> revenueByProduct = byProduct.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "productName", e.getKey(),
                        "revenue", e.getValue().revenue,
                        "quantity", e.getValue().quantity
                ))
                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        List<Map<String, Object>> revenueByCustomer = byCustomer.entrySet().stream()
                .map(e -> {
                    CustomerAgg c = e.getValue();
                    return Map.<String, Object>of(
                            "customerKey", e.getKey(),
                            "customerName", Objects.toString(c.customerName, ""),
                            "customerEmail", Objects.toString(c.customerEmail, ""),
                            "revenue", c.revenue,
                            "orders", c.orders
                    );
                })
                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        Map<String, Object> response = Map.of(
                "summary", Map.of(
                        "totalOrders", orders.size(),
                        "deliveredOrders", deliveredOrders,
                        "totalRevenue", totalRevenue,
                        "deliveredRevenue", deliveredRevenue
                ),
                "revenueByCategory", revenueByCategory,
                "revenueByProduct", revenueByProduct,
                "revenueByCustomer", revenueByCustomer
        );
        return ResponseEntity.ok(response);
    }

    /**
     * KPI, xu hướng so với kỳ trước (cùng độ dài), timeline doanh thu & đơn theo ngày.
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analytics(
            @RequestParam(required = false, defaultValue = "30") Integer days,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<Order> all = orderRepository.findAll();
        TimeRange current = resolveAnalyticsRange(days, from, to);
        List<Order> curOrders = filterOrdersInRange(all, current.start(), current.end());

        long spanSeconds = Math.max(ChronoUnit.SECONDS.between(current.start(), current.end()), 86400L);
        Instant prevEnd = current.start();
        Instant prevStart = prevEnd.minusSeconds(spanSeconds);
        List<Order> prevOrders = filterOrdersInRange(all, prevStart, prevEnd);

        BigDecimal revCur = sumOrderTotals(curOrders);
        BigDecimal revPrev = sumOrderTotals(prevOrders);
        int ordCur = curOrders.size();
        int ordPrev = prevOrders.size();
        long custCur = countDistinctCustomers(curOrders);
        long custPrev = countDistinctCustomers(prevOrders);
        long delCur = countDelivered(curOrders);
        long delPrev = countDelivered(prevOrders);
        double convCur = ordCur == 0 ? 0.0 : (100.0 * delCur / ordCur);
        double convPrev = ordPrev == 0 ? 0.0 : (100.0 * delPrev / ordPrev);

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("revenue", revCur);
        kpis.put("orders", ordCur);
        kpis.put("customers", custCur);
        kpis.put("conversionRate", BigDecimal.valueOf(convCur).setScale(1, RoundingMode.HALF_UP));

        Map<String, Object> trends = new HashMap<>();
        trends.put("revenuePct", pctChange(revCur, revPrev));
        trends.put("ordersPct", pctChangeInt(ordCur, ordPrev));
        trends.put("customersPct", pctChangeLong(custCur, custPrev));
        trends.put("conversionPts", BigDecimal.valueOf(convCur - convPrev).setScale(1, RoundingMode.HALF_UP));

        List<Map<String, Object>> timeline = buildDailyTimeline(curOrders, current.start(), current.end());

        Map<String, Object> rangeMeta = new HashMap<>();
        rangeMeta.put("start", current.start().toString());
        rangeMeta.put("end", current.end().toString());

        Map<String, Object> body = new HashMap<>();
        body.put("kpis", kpis);
        body.put("trends", trends);
        body.put("timeline", timeline);
        body.put("range", rangeMeta);
        return ResponseEntity.ok(body);
    }

    private record TimeRange(Instant start, Instant end) {}

    private static TimeRange resolveReportRange(Integer days, String fromIso, String toIso) {
        try {
            if (fromIso != null && !fromIso.isBlank() && toIso != null && !toIso.isBlank()) {
                Instant fs = Instant.parse(fromIso.trim());
                Instant te = Instant.parse(toIso.trim());
                if (fs.isBefore(te)) return new TimeRange(fs, te);
            }
        } catch (Exception ignored) { }
        if (days != null && days > 0) {
            int d = Math.min(days, 365);
            Instant end = Instant.now();
            return new TimeRange(end.minus(d, ChronoUnit.DAYS), end);
        }
        return null;
    }

    private static TimeRange resolveAnalyticsRange(Integer daysParam, String from, String to) {
        try {
            if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
                Instant fs = Instant.parse(from.trim());
                Instant te = Instant.parse(to.trim());
                if (fs.isBefore(te)) return new TimeRange(fs, te);
            }
        } catch (Exception ignored) { }
        int d = (daysParam == null || daysParam < 1) ? 30 : Math.min(daysParam, 365);
        Instant end = Instant.now();
        return new TimeRange(end.minus(d, ChronoUnit.DAYS), end);
    }

    private static List<Order> filterOrdersInRange(List<Order> all, Instant start, Instant end) {
        return all.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    Instant t = o.getCreatedAt();
                    return !t.isBefore(start) && !t.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    private static BigDecimal sumOrderTotals(List<Order> orders) {
        return orders.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static long countDelivered(List<Order> orders) {
        return orders.stream().filter(o -> "delivered".equalsIgnoreCase(o.getStatus())).count();
    }

    private static long countDistinctCustomers(List<Order> orders) {
        Set<String> set = new HashSet<>();
        for (Order o : orders) {
            if (o.getUserId() != null) {
                set.add("user:" + o.getUserId());
            } else if (o.getCustomerEmail() != null && !o.getCustomerEmail().isBlank()) {
                set.add("email:" + o.getCustomerEmail().trim().toLowerCase());
            } else {
                String name = o.getCustomerName() != null ? o.getCustomerName().trim().toLowerCase() : "guest";
                set.add("name:" + name);
            }
        }
        return set.size();
    }

    private static double pctChange(BigDecimal cur, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            return cur.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return cur.subtract(prev).multiply(BigDecimal.valueOf(100))
                .divide(prev, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private static double pctChangeInt(int cur, int prev) {
        if (prev == 0) return cur > 0 ? 100.0 : 0.0;
        return (100.0 * (cur - prev) / prev);
    }

    private static double pctChangeLong(long cur, long prev) {
        if (prev == 0) return cur > 0 ? 100.0 : 0.0;
        return (100.0 * (cur - prev) / prev);
    }

    private static List<Map<String, Object>> buildDailyTimeline(List<Order> orders, Instant start, Instant end) {
        ZoneId z = ZoneId.systemDefault();
        LocalDate startD = start.atZone(z).toLocalDate();
        LocalDate endD = end.atZone(z).toLocalDate();

        Map<String, BigDecimal> rev = new TreeMap<>();
        Map<String, Integer> ord = new TreeMap<>();
        for (LocalDate d = startD; !d.isAfter(endD); d = d.plusDays(1)) {
            String key = d.toString();
            rev.put(key, BigDecimal.ZERO);
            ord.put(key, 0);
        }
        for (Order o : orders) {
            if (o.getCreatedAt() == null) continue;
            LocalDate d = o.getCreatedAt().atZone(z).toLocalDate();
            String key = d.toString();
            if (!rev.containsKey(key)) continue;
            rev.merge(key, o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO, BigDecimal::add);
            ord.merge(key, 1, Integer::sum);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (String k : rev.keySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", k);
            row.put("revenue", rev.get(k));
            row.put("orders", ord.get(k));
            out.add(row);
        }
        return out;
    }

    // ==========================================
    // 2. QUẢN LÝ SẢN PHẨM (phân trang & lọc; mới nhất lên đầu)
    // ==========================================
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.filterProducts(keyword, category, brand, minPrice, maxPrice), pageable);
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (product.getPrice() == null) product.setPrice(java.math.BigDecimal.ZERO);
        if (product.getStockQty() == null) product.setStockQty(0);
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product body) {
        return productRepository.findById(id)
                .map(p -> {
                    p.setName(body.getName());
                    p.setPrice(body.getPrice());
                    p.setDescription(body.getDescription());
                    p.setBrand(body.getBrand());
                    p.setCategoryId(body.getCategoryId());
                    p.setCategory(body.getCategory());
                    p.setImageUrl(body.getImageUrl());
                    if (body.getStockQty() != null) p.setStockQty(body.getStockQty());
                    return ResponseEntity.ok(productRepository.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 3. QUẢN LÝ ĐƠN HÀNG 
    // ==========================================
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> listOrders() {
        return ResponseEntity.ok(orderRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/orders/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String cancelReason = body.get("cancelReason");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
        Order o = orderRepository.findById(id).orElse(null);
        if (o == null) return ResponseEntity.notFound().build();

        String nextStatus = status.trim().toLowerCase();
        String prevStatus = o.getStatus() == null ? "" : o.getStatus().trim().toLowerCase();

        if (!prevStatus.equals(nextStatus)) {
            String stockError = null;
            if (!"delivered".equals(prevStatus) && "delivered".equals(nextStatus)) {
                stockError = applyDeliveredStock(o, -1);
            } else if ("delivered".equals(prevStatus) && !"delivered".equals(nextStatus)) {
                stockError = applyDeliveredStock(o, 1);
            }
            if (stockError != null) {
                return ResponseEntity.badRequest().body(Map.of("message", stockError));
            }
        }

        if ("cancelled".equals(nextStatus)) {
            o.setCancelReason(cancelReason);
        }

        o.setStatus(nextStatus);
        return ResponseEntity.ok(orderRepository.save(o));
    }

    private String applyDeliveredStock(Order order, int direction) {
        if (order.getItems() == null || order.getItems().isEmpty()) return null;
        List<Product> touched = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (qty <= 0) continue;

            Product product = resolveProductForOrderItem(item);
            if (product == null) {
                return "Không tìm thấy sản phẩm để cập nhật tồn kho cho mục: " + item.getProductName();
            }

            int current = product.getStockQty() == null ? 0 : product.getStockQty();
            int next = current + (direction * qty);
            if (next < 0) {
                return "Tồn kho không đủ cho sản phẩm: " + product.getName();
            }
            product.setStockQty(next);
            touched.add(product);
        }
        if (!touched.isEmpty()) {
            productRepository.saveAll(touched);
        }
        return null;
    }

    private Product resolveProductForOrderItem(OrderItem item) {
        if (item.getProductId() != null) {
            return productRepository.findById(item.getProductId()).orElse(null);
        }
        if (item.getProductName() == null || item.getProductName().isBlank()) {
            return null;
        }
        return productRepository.findByNameContainingIgnoreCase(item.getProductName()).stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(item.getProductName().trim()))
                .findFirst()
                .orElse(null);
    }

    // ==========================================
    // CÁC CLASS HỖ TRỢ DOANH THU
    // ==========================================
    private static class RevenueAgg {
        private BigDecimal revenue = BigDecimal.ZERO;
        private int quantity = 0;

        void add(BigDecimal amount, int qty) {
            revenue = revenue.add(amount != null ? amount : BigDecimal.ZERO);
            quantity += Math.max(0, qty);
        }
    }

    private static class CustomerAgg {
        private final String customerName;
        private final String customerEmail;
        private BigDecimal revenue = BigDecimal.ZERO;
        private int orders = 0;

        CustomerAgg(String customerName, String customerEmail) {
            this.customerName = customerName;
            this.customerEmail = customerEmail;
        }

        void add(BigDecimal amount) {
            revenue = revenue.add(amount != null ? amount : BigDecimal.ZERO);
            orders++;
        }
    }
}