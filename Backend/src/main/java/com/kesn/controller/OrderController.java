package com.kesn.controller;

import com.kesn.dto.CreateOrderRequest;
import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;
import com.kesn.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateOrderRequest req) {
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
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", saved.getStatus(), "total", saved.getTotal()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}
