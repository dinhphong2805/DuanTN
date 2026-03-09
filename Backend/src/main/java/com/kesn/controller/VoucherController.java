package com.kesn.controller;

import com.kesn.entity.Voucher;
import com.kesn.repository.VoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class VoucherController {

    private final VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    /** Validate voucher - public API for checkout */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "0") Double total) {
        var v = voucherRepository.findByCodeIgnoreCase(code.trim());
        if (v.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Mã không tồn tại"
            ));
        }
        Voucher voucher = v.get();
        BigDecimal minOrder = voucher.getMinOrder() != null ? voucher.getMinOrder() : BigDecimal.ZERO;
        if (BigDecimal.valueOf(total).compareTo(minOrder) < 0) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Đơn tối thiểu " + minOrder.longValue() + "đ"
            ));
        }
        BigDecimal amount;
        if ("fixed".equalsIgnoreCase(voucher.getType())) {
            amount = voucher.getDiscount() != null ? voucher.getDiscount() : BigDecimal.ZERO;
        } else {
            amount = BigDecimal.valueOf(total).multiply(
                    voucher.getDiscount() != null ? voucher.getDiscount() : BigDecimal.ZERO
            ).divide(BigDecimal.valueOf(100));
        }
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "voucher", Map.of(
                        "code", voucher.getCode(),
                        "discount", voucher.getDiscount(),
                        "type", voucher.getType(),
                        "minOrder", voucher.getMinOrder() != null ? voucher.getMinOrder() : 0
                ),
                "amount", amount
        ));
    }
}
