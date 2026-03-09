package com.kesn.controller;

import com.kesn.entity.Voucher;
import com.kesn.repository.VoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vouchers")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AdminVoucherController {

    private final VoucherRepository voucherRepository;

    public AdminVoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping
    public ResponseEntity<List<Voucher>> list() {
        return ResponseEntity.ok(voucherRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã không được để trống"));
        }
        if (voucherRepository.existsByCodeIgnoreCase(code.trim())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mã đã tồn tại"));
        }
        Voucher v = new Voucher();
        v.setCode(code.trim().toUpperCase());
        v.setDiscount(toBigDecimal(body.get("discount"), BigDecimal.ZERO));
        v.setType(body.containsKey("type") ? (String) body.get("type") : "percent");
        v.setMinOrder(toBigDecimal(body.get("minOrder"), BigDecimal.ZERO));
        return ResponseEntity.ok(voucherRepository.save(v));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!voucherRepository.existsById(id)) return ResponseEntity.notFound().build();
        voucherRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private static BigDecimal toBigDecimal(Object o, BigDecimal fallback) {
        if (o == null) return fallback;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        try {
            return new BigDecimal(o.toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
