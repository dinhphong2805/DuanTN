package com.kesn.controller;

import com.kesn.entity.Order;
import com.kesn.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class WebhookController {

    private final OrderRepository orderRepository;

    public WebhookController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * VNPAY IPN: API này để VNPAY gọi Server-to-Server.
     * Ngay cả khi khách tắt trình duyệt, đơn hàng vẫn được cập nhật 'paid'.
     */
    @GetMapping("/vnpay-ipn")
    public String vnpayIPN(@RequestParam Map<String, String> params) {
        try {
            // 1. Lấy các thông số cần thiết
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef"); // Đây chính là Order ID của bạn
            
            // 2. Kiểm tra xem đơn hàng có tồn tại không
            Optional<Order> orderOpt = orderRepository.findById(Long.parseLong(vnp_TxnRef));
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                
                // 3. Kiểm tra trạng thái đơn hàng (Chỉ xử lý nếu đang pending)
                if (!"pending".equals(order.getStatus())) {
                    return "{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}";
                }

                // 4. Kiểm tra mã phản hồi (00 là thành công)
                if ("00".equals(vnp_ResponseCode)) {
                    order.setStatus("paid");
                    orderRepository.save(order);
                    // Trả về định dạng JSON mà VNPAY yêu cầu
                    return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
                } else {
                    // Nếu khách hủy hoặc lỗi, bạn có thể để 'failed' hoặc giữ 'pending'
                    return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success (Payment Failed)\"}";
                }
            } else {
                return "{\"RspCode\":\"01\",\"Message\":\"Order not found\"}";
            }
        } catch (Exception e) {
            return "{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}";
        }
    }
}