//package com.kesn.controller;
//
//import com.kesn.dto.AdminStatsDto;
//import com.kesn.entity.Order;
//import com.kesn.entity.OrderItem;
//import com.kesn.entity.Product;
//import com.kesn.repository.OrderRepository;
//import com.kesn.repository.ProductRepository;
//import com.kesn.repository.UserRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/admin")
//@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
//public class AdminController {
//
//    private final ProductRepository productRepository;
//    private final OrderRepository orderRepository;
//    private final UserRepository userRepository;
//
//    public AdminController(ProductRepository productRepository, OrderRepository orderRepository, UserRepository userRepository) {
//        this.productRepository = productRepository;
//        this.orderRepository = orderRepository;
//        this.userRepository = userRepository;
//    }
//
//    @GetMapping("/stats")
//    public ResponseEntity<AdminStatsDto> stats() {
//        long products = productRepository.count();
//        long orders = orderRepository.count();
//        long users = userRepository.count();
//        return ResponseEntity.ok(new AdminStatsDto(products, orders, users));
//    }
//
//    @GetMapping("/revenue-report")
//    public ResponseEntity<Map<String, Object>> revenueReport(
//            @RequestParam(required = false, defaultValue = "10") Integer limit) {
//        int topN = Math.max(1, Math.min(limit == null ? 10 : limit, 100));
//        List<Order> orders = orderRepository.findAll();
//
//        BigDecimal totalRevenue = orders.stream()
//                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        long deliveredOrders = orders.stream()
//                .filter(o -> "delivered".equalsIgnoreCase(o.getStatus()))
//                .count();
//        BigDecimal deliveredRevenue = orders.stream()
//                .filter(o -> "delivered".equalsIgnoreCase(o.getStatus()))
//                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        Map<String, String> categoryByProductName = productRepository.findAll().stream()
//                .filter(p -> p.getName() != null && !p.getName().isBlank())
//                .collect(Collectors.toMap(
//                        p -> p.getName().trim().toLowerCase(),
//                        p -> (p.getCategory() == null || p.getCategory().isBlank()) ? "Khong xac dinh" : p.getCategory().trim(),
//                        (a, b) -> a
//                ));
//
//        Map<String, RevenueAgg> byCategory = new HashMap<>();
//        Map<String, RevenueAgg> byProduct = new HashMap<>();
//        Map<String, CustomerAgg> byCustomer = new HashMap<>();
//
//        for (Order order : orders) {
//            if (order.getItems() != null) {
//                for (OrderItem item : order.getItems()) {
//                    String productName = (item.getProductName() == null || item.getProductName().isBlank())
//                            ? "Khong xac dinh"
//                            : item.getProductName().trim();
//                    int qty = item.getQuantity() != null ? item.getQuantity() : 0;
//                    BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
//                    BigDecimal lineRevenue = unitPrice.multiply(BigDecimal.valueOf(qty));
//
//                    String category = categoryByProductName.getOrDefault(productName.trim().toLowerCase(), "Khong xac dinh");
//                    byCategory.computeIfAbsent(category, k -> new RevenueAgg()).add(lineRevenue, qty);
//                    byProduct.computeIfAbsent(productName, k -> new RevenueAgg()).add(lineRevenue, qty);
//                }
//            }
//
//            String customerKey;
//            if (order.getUserId() != null) {
//                customerKey = "user:" + order.getUserId();
//            } else if (order.getCustomerEmail() != null && !order.getCustomerEmail().isBlank()) {
//                customerKey = "email:" + order.getCustomerEmail().trim().toLowerCase();
//            } else {
//                String name = order.getCustomerName() != null ? order.getCustomerName().trim().toLowerCase() : "guest";
//                customerKey = "name:" + name;
//            }
//            byCustomer.computeIfAbsent(customerKey, k -> new CustomerAgg(order.getCustomerName(), order.getCustomerEmail()))
//                    .add(order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO);
//        }
//
//        List<Map<String, Object>> revenueByCategory = byCategory.entrySet().stream()
//                .map(e -> Map.<String, Object>of(
//                        "category", e.getKey(),
//                        "revenue", e.getValue().revenue,
//                        "quantity", e.getValue().quantity
//                ))
//                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
//                .limit(topN)
//                .collect(Collectors.toList());
//
//        List<Map<String, Object>> revenueByProduct = byProduct.entrySet().stream()
//                .map(e -> Map.<String, Object>of(
//                        "productName", e.getKey(),
//                        "revenue", e.getValue().revenue,
//                        "quantity", e.getValue().quantity
//                ))
//                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
//                .limit(topN)
//                .collect(Collectors.toList());
//
//        List<Map<String, Object>> revenueByCustomer = byCustomer.entrySet().stream()
//                .map(e -> {
//                    CustomerAgg c = e.getValue();
//                    return Map.<String, Object>of(
//                            "customerKey", e.getKey(),
//                            "customerName", Objects.toString(c.customerName, ""),
//                            "customerEmail", Objects.toString(c.customerEmail, ""),
//                            "revenue", c.revenue,
//                            "orders", c.orders
//                    );
//                })
//                .sorted(Comparator.comparing((Map<String, Object> m) -> (BigDecimal) m.get("revenue")).reversed())
//                .limit(topN)
//                .collect(Collectors.toList());
//
//        Map<String, Object> response = Map.of(
//                "summary", Map.of(
//                        "totalOrders", orders.size(),
//                        "deliveredOrders", deliveredOrders,
//                        "totalRevenue", totalRevenue,
//                        "deliveredRevenue", deliveredRevenue
//                ),
//                "revenueByCategory", revenueByCategory,
//                "revenueByProduct", revenueByProduct,
//                "revenueByCustomer", revenueByCustomer
//        );
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/products")
//    public ResponseEntity<List<Product>> listProducts() {
//        return ResponseEntity.ok(productRepository.findAll());
//    }
//
//    @GetMapping("/products/{id}")
//    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
//        return productRepository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PostMapping("/products")
//    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
//        if (product.getName() == null || product.getName().isBlank()) {
//            return ResponseEntity.badRequest().build();
//        }
//        if (product.getPrice() == null) product.setPrice(java.math.BigDecimal.ZERO);
//        Product saved = productRepository.save(product);
//        return ResponseEntity.ok(saved);
//    }
//
//    @PutMapping("/products/{id}")
//    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product body) {
//        return productRepository.findById(id)
//                .map(p -> {
//                    p.setName(body.getName());
//                    p.setPrice(body.getPrice());
//                    p.setDescription(body.getDescription());
//                    p.setBrand(body.getBrand());
//                    p.setCategory(body.getCategory());
//                    p.setImageUrl(body.getImageUrl());
//                    return ResponseEntity.ok(productRepository.save(p));
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @DeleteMapping("/products/{id}")
//    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
//        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
//        productRepository.deleteById(id);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/orders")
//    public ResponseEntity<List<Order>> listOrders() {
//        return ResponseEntity.ok(orderRepository.findAll());
//    }
//
//    @GetMapping("/orders/{id}")
//    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
//        return orderRepository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @PatchMapping("/orders/{id}/status")
//    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
//        String status = body.get("status");
//        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
//        return orderRepository.findById(id)
//                .map(o -> {
//                    o.setStatus(status);
//                    return ResponseEntity.ok(orderRepository.save(o));
//                })
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    private static class RevenueAgg {
//        private BigDecimal revenue = BigDecimal.ZERO;
//        private int quantity = 0;
//
//        void add(BigDecimal amount, int qty) {
//            revenue = revenue.add(amount != null ? amount : BigDecimal.ZERO);
//            quantity += Math.max(0, qty);
//        }
//    }
//
//    private static class CustomerAgg {
//        private final String customerName;
//        private final String customerEmail;
//        private BigDecimal revenue = BigDecimal.ZERO;
//        private int orders = 0;
//
//        CustomerAgg(String customerName, String customerEmail) {
//            this.customerName = customerName;
//            this.customerEmail = customerEmail;
//        }
//
//        void add(BigDecimal amount) {
//            revenue = revenue.add(amount != null ? amount : BigDecimal.ZERO);
//            orders++;
//        }
//    }
//}





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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Mở CrossOrigin cho mượt
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
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        int topN = Math.max(1, Math.min(limit == null ? 10 : limit, 100));
        List<Order> orders = orderRepository.findAll();

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

    // ==========================================
    // 2. QUẢN LÝ SẢN PHẨM (Đã nâng cấp Phân trang & Lọc)
    // ==========================================
    
    // API lấy danh sách CÓ PHÂN TRANG VÀ LỌC
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
        // ĐÃ SỬA: Đổi .descending() thành .ascending() để xếp theo thứ tự ID từ bé đến lớn (1, 2, 3...)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending()); 
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
                    p.setCategory(body.getCategory());
                    p.setImageUrl(body.getImageUrl());
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
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
        return orderRepository.findById(id)
                .map(o -> {
                    o.setStatus(status);
                    return ResponseEntity.ok(orderRepository.save(o));
                })
                .orElse(ResponseEntity.notFound().build());
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