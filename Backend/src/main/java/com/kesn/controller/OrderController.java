package com.kesn.controller;

import com.kesn.dto.OrderDetailResponse;
import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;
import com.kesn.repository.OrderRepository;
import com.kesn.service.VNPAYService;
import com.kesn.dto.CreateOrderRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final VNPAYService vnpayService;
    private final com.kesn.repository.VoucherRepository voucherRepository;
    private final com.kesn.repository.UserVoucherRepository userVoucherRepository;

    public OrderController(OrderRepository orderRepository, VNPAYService vnpayService, 
                           com.kesn.repository.VoucherRepository voucherRepository,
                           com.kesn.repository.UserVoucherRepository userVoucherRepository) {
        this.orderRepository = orderRepository;
        this.vnpayService = vnpayService;
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateOrderRequest req, HttpServletRequest request) {
        try {
            Order order = new Order();
            order.setUserId(req.getUserId());
            order.setCustomerName(req.getCustomerName());
            order.setCustomerEmail(req.getCustomerEmail());
            order.setCustomerPhone(req.getCustomerPhone());
            order.setAddress(req.getAddress());
            order.setStatus("pending");

            java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
            if (req.getItems() != null) {
                for (CreateOrderRequest.OrderItemDto dto : req.getItems()) {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductId(dto.getProductId());
                    item.setProductName(dto.getProductName());
                    item.setQuantity(dto.getQuantity());
                    item.setUnitPrice(dto.getUnitPrice());
                    order.getItems().add(item);
                    subtotal = subtotal.add(dto.getUnitPrice().multiply(java.math.BigDecimal.valueOf(dto.getQuantity())));
                }
            }

            java.math.BigDecimal total = subtotal;
            if (req.getVoucherCode() != null && !req.getVoucherCode().isBlank()) {
                var vOpt = voucherRepository.findByCodeIgnoreCase(req.getVoucherCode().trim());
                if (vOpt.isPresent()) {
                    com.kesn.entity.Voucher voucher = vOpt.get();
                    java.time.Instant now = java.time.Instant.now();
                    
                    boolean isValid = "active".equalsIgnoreCase(voucher.getStatus()) &&
                        (voucher.getStartAt() == null || now.isAfter(voucher.getStartAt()) || now.equals(voucher.getStartAt())) &&
                        (voucher.getEndAt() == null || now.isBefore(voucher.getEndAt()) || now.equals(voucher.getEndAt())) &&
                        (voucher.getMinOrder() == null || subtotal.compareTo(voucher.getMinOrder()) >= 0);
                        
                    if (isValid) {
                        boolean canUse = true;
                        if (req.getUserId() != null) {
                            var uvOpt = userVoucherRepository.findByUserIdAndVoucherCode(req.getUserId(), voucher.getCode());
                            if (uvOpt.isPresent()) {
                                com.kesn.entity.UserVoucher uv = uvOpt.get();
                                if (uv.getIsUsed()) canUse = false;
                                else {
                                    uv.setIsUsed(true);
                                    uv.setUsedAt(now);
                                    userVoucherRepository.save(uv);
                                }
                            }
                        }
                        
                        if (canUse) {
                            java.math.BigDecimal discountAmount;
                            if ("fixed".equalsIgnoreCase(voucher.getType())) {
                                discountAmount = voucher.getDiscount() != null ? voucher.getDiscount() : java.math.BigDecimal.ZERO;
                            } else {
                                discountAmount = subtotal.multiply(
                                        voucher.getDiscount() != null ? voucher.getDiscount() : java.math.BigDecimal.ZERO
                                ).divide(java.math.BigDecimal.valueOf(100));
                            }
                            
                            if (discountAmount.compareTo(subtotal) > 0) discountAmount = subtotal;
                            total = subtotal.subtract(discountAmount);
                            
                            order.setVoucherCode(voucher.getCode());
                            order.setDiscountAmount(discountAmount);
                        }
                    }
                }
            }

            order.setTotal(total);

            Order saved = orderRepository.save(order);
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("status", saved.getStatus());

            if ("bank".equalsIgnoreCase(req.getPaymentMethod())) {
                String paymentUrl = vnpayService.createPaymentUrl(saved, request);
                response.put("paymentUrl", paymentUrl);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi tạo thanh toán: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDetailResponse>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderDetailResponse> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderDetailResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/detail/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId, @RequestParam(required = false) Long userId) {
        var orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        
        Order order = orderOpt.get();
        // Kiểm tra quyền sở hữu nếu truyền userId lên (User thông thường)
        // Nếu không có userId truyền lên, có thể là Admin truy cập
        if (userId != null && !userId.equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Đơn hàng không thuộc tài khoản của bạn"));
        }
        
        return ResponseEntity.ok(OrderDetailResponse.fromEntity(order));
    }
}