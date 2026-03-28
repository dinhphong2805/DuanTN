// File: com/kesn/controller/WebhookController.java
package com.kesn.controller;

import com.kesn.dto.WebhookRequest;
import com.kesn.entity.Order;
import com.kesn.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/payments")
public class WebhookController {

    private final OrderRepository orderRepository;

    public WebhookController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // API này sẽ được bên thứ 3 (SePay/Casso) gọi tự động khi tài khoản ngân hàng có tiền vào
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookRequest req) {
        
        // 1. Chỉ xử lý khi có dòng tiền VÀO tài khoản
        if (!"in".equalsIgnoreCase(req.getTransferType())) {
            return ResponseEntity.ok("Bỏ qua giao dịch trừ tiền");
        }

        // Nội dung khách ghi khi chuyển (VD: "NGUYEN VAN A chuyen tien DH123")
        String transferContent = req.getContent() != null ? req.getContent().toUpperCase() : "";
        
        // 2. Dùng Regex để tìm mã đơn hàng bắt đầu bằng "DH" kèm theo các chữ số
        Pattern pattern = Pattern.compile("DH(\\d+)");
        Matcher matcher = pattern.matcher(transferContent);

        if (matcher.find()) {
            // Lấy ra phần số (VD: lấy ra 123 từ DH123)
            Long orderId = Long.parseLong(matcher.group(1));
            
            // Tìm đơn hàng trong Database
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                
                // 3. Kiểm tra: Đơn đang pending VÀ khách chuyển ĐỦ hoặc DƯ tiền không?
                if ("pending".equals(order.getStatus()) && 
                    req.getTransferAmount().compareTo(order.getTotal()) >= 0) {
                    
                    // CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG THÀNH "PAID"
                    order.setStatus("paid"); 
                    orderRepository.save(order);
                    
                    return ResponseEntity.ok("Thành công: Đã cập nhật trạng thái đơn hàng " + orderId);
                }
            }
        }
        
        return ResponseEntity.ok("Giao dịch đã ghi nhận nhưng không tìm thấy đơn hàng hợp lệ");
    }
}