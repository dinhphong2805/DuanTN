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
     * VNPAY API này để VNPAY gọi Server-to-Server.
     */
    @GetMapping("/vnpay-ipn")
    public String vnpayIPN(@RequestParam Map<String, String> params) {
        try {
            // Lấy các thông số cần thiết
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef"); 
            
            // Kiểm tra xem đơn hàng có tồn tại không
            Optional<Order> orderOpt = orderRepository.findById(Long.parseLong(vnp_TxnRef));
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                
                // Kiểm tra trạng thái đơn hàng
                if (!"pending".equals(order.getStatus())) {
                    return "{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}";
                }

                // Kiểm tra mã phản hồi
                if ("00".equals(vnp_ResponseCode)) {
                    order.setStatus("paid");
                    orderRepository.save(order);
          
                    return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
                } else {
            
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