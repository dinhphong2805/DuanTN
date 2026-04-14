package com.kesn.controller;

import com.kesn.dto.ImportReceiptRequest;
import com.kesn.entity.ImportReceipt;
import com.kesn.service.ImportReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import-receipts")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174"})
public class AdminImportReceiptController {
    private final ImportReceiptService importReceiptService;

    public AdminImportReceiptController(ImportReceiptService importReceiptService) {
        this.importReceiptService = importReceiptService;
    }

    @GetMapping
    public ResponseEntity<List<ImportReceipt>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(importReceiptService.findAll(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportReceipt> detail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(importReceiptService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ImportReceiptRequest request) {
        try {
            return ResponseEntity.ok(importReceiptService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ImportReceiptRequest request) {
        try {
            return ResponseEntity.ok(importReceiptService.update(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            importReceiptService.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
