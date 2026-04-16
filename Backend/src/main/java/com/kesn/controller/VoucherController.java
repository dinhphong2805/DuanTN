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
    private final com.kesn.repository.UserVoucherRepository userVoucherRepository;

    public VoucherController(VoucherRepository voucherRepository, com.kesn.repository.UserVoucherRepository userVoucherRepository) {
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    /** Validate voucher - public API for checkout */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "0") Double total,
            @RequestParam(required = false) Long userId) {
        var v = voucherRepository.findByCodeIgnoreCase(code.trim());
        if (v.isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Mã không tồn tại"));
        }
        Voucher voucher = v.get();
        if (!"active".equalsIgnoreCase(voucher.getStatus())) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Mã không hoạt động"));
        }
        java.time.Instant now = java.time.Instant.now();
        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Mã chưa đến thời gian áp dụng"));
        }
        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Mã đã hết hạn"));
        }
        
        if (userId != null) {
            var uv = userVoucherRepository.findByUserIdAndVoucherCode(userId, voucher.getCode());
            if (uv.isPresent() && uv.get().getIsUsed()) {
                return ResponseEntity.ok(Map.of("valid", false, "message", "Bạn đã sử dụng mã này rồi"));
            }
        }

        BigDecimal minOrder = voucher.getMinOrder() != null ? voucher.getMinOrder() : BigDecimal.ZERO;
        if (BigDecimal.valueOf(total).compareTo(minOrder) < 0) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Đơn tối thiểu " + minOrder.longValue() + "đ"));
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

    @GetMapping("/my-vouchers")
    public ResponseEntity<List<Map<String, Object>>> myVouchers(@RequestParam Long userId) {
        List<com.kesn.entity.UserVoucher> userVouchers = userVoucherRepository.findByUserIdAndIsUsedFalse(userId);
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.Instant now = java.time.Instant.now();
        
        for (com.kesn.entity.UserVoucher uv : userVouchers) {
            Voucher v = uv.getVoucher();
            if ("active".equalsIgnoreCase(v.getStatus()) &&
               (v.getEndAt() == null || now.isBefore(v.getEndAt()))) {
                result.add(Map.of(
                    "id", v.getId(),
                    "code", v.getCode(),
                    "discount", v.getDiscount(),
                    "type", v.getType(),
                    "minOrder", v.getMinOrder() != null ? v.getMinOrder() : 0,
                    "endAt", v.getEndAt() != null ? v.getEndAt().toString() : ""
                ));
            }
        }
        return ResponseEntity.ok(result);
    }
}
