package com.kesn.controller;

import com.kesn.dto.CreateOrderRequest;
import com.kesn.dto.OrderDetailResponse;
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

        // --- THÊM LOGIC TẠO LINK QR TẠI ĐÂY ---
        // Thông tin tài khoản nhận tiền (Bro nhớ sửa lại thành của bro)
        String bankId = "MB"; // Mã ngân hàng: MB, VCB, ACB, TPB...
        String accountNo = "4628112005"; // Số tài khoản
        String accountName = "NGUYEN TRUNG NGUYEN"; // Tên chủ tài khoản (Viết không dấu)
        
        // Nội dung chuyển khoản: Để tránh trùng lặp, dùng tiền tố DH + ID đơn hàng
        String addInfo = "DH" + saved.getId(); 
        
        // Số tiền chuyển (Bỏ phần thập phân nếu có)
        String amount = String.valueOf(saved.getTotal().longValue());

        // Tạo link VietQR động
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
            bankId, accountNo, amount, addInfo, accountName.replace(" ", "%20")
        );

        // Trả thêm qrUrl về cho Vue
        return ResponseEntity.ok(Map.of(
            "id", saved.getId(), 
            "status", saved.getStatus(), 
            "total", saved.getTotal(),
            "qrUrl", qrUrl // Dữ liệu mới thêm
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** Chi tiết đơn (DTO) — chỉ trả về nếu đơn thuộc đúng user (khớp user_id). */
    @GetMapping("/detail/{id}")
    public ResponseEntity<OrderDetailResponse> getDetailForUser(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return orderRepository.findById(id)
                .filter(o -> o.getUserId() != null && o.getUserId().equals(userId))
                .map(OrderDetailResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

 // API để hủy đơn hàng (xóa khỏi DB) khi khách không muốn thanh toán QR nữa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng"));
    }
    
 // API kiểm tra trạng thái của 1 đơn hàng (dành cho Frontend tự động gọi)
    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getOrderStatus(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(Map.of("status", order.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }
}
