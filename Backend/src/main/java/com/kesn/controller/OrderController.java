package com.kesn.controller;

import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;
import com.kesn.repository.OrderRepository;
import com.kesn.service.VNPAYService;
import com.kesn.dto.CreateOrderRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final VNPAYService vnpayService;

    public OrderController(OrderRepository orderRepository, VNPAYService vnpayService) {
        this.orderRepository = orderRepository;
        this.vnpayService = vnpayService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateOrderRequest req, HttpServletRequest request) {
        try {
            Order order = new Order();
            order.setUserId(req.getUserId());
            order.setCustomerName(req.getCustomerName());
            order.setCustomerEmail(req.getCustomerEmail());
            order.setCustomerPhone(req.getCustomerPhone());
            order.setAddress(req.getAddress());
            order.setTotal(req.getTotal());
            order.setStatus("pending");

            if (req.getItems() != null) {
                for (CreateOrderRequest.OrderItemDto dto : req.getItems()) {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductName(dto.getProductName());
                    item.setQuantity(dto.getQuantity());
                    item.setUnitPrice(dto.getUnitPrice());
                    order.getItems().add(item);
                }
            }

            Order saved = orderRepository.save(order);

            // TẠO LINK THANH TOÁN VNPAY
            String paymentUrl = vnpayService.createPaymentUrl(saved, request);

            return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "status", saved.getStatus(),
                "paymentUrl", paymentUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi tạo thanh toán: " + e.getMessage()));
        }
    }
}