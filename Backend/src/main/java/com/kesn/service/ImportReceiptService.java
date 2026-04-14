package com.kesn.service;

import com.kesn.dto.ImportReceiptRequest;
import com.kesn.entity.ImportReceipt;
import com.kesn.entity.ImportReceiptItem;
import com.kesn.entity.Product;
import com.kesn.repository.ImportReceiptRepository;
import com.kesn.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImportReceiptService {
    private final ImportReceiptRepository receiptRepository;
    private final ProductRepository productRepository;

    public ImportReceiptService(ImportReceiptRepository receiptRepository, ProductRepository productRepository) {
        this.receiptRepository = receiptRepository;
        this.productRepository = productRepository;
    }

    public List<ImportReceipt> findAll(String keyword) {
        List<ImportReceipt> list = receiptRepository.findAllByOrderByCreatedAtDesc();
        if (keyword == null || keyword.isBlank()) return list;
        String k = keyword.trim().toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(r ->
                        contains(r.getCode(), k)
                                || contains(r.getSupplierName(), k)
                                || contains(r.getCreatedBy(), k)
                )
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    public ImportReceipt findById(Long id) {
        return receiptRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập."));
    }

    @Transactional
    public ImportReceipt create(ImportReceiptRequest req) {
        validateBasic(req, null);
        ImportReceipt receipt = new ImportReceipt();
        applyFields(receipt, req);
        applyItemsAndStock(receipt, req);
        return receiptRepository.save(receipt);
    }

    @Transactional
    public ImportReceipt update(Long id, ImportReceiptRequest req) {
        ImportReceipt existing = findById(id);
        validateBasic(req, id);

        rollbackStock(existing.getItems());
        existing.getItems().clear();

        applyFields(existing, req);
        applyItemsAndStock(existing, req);
        return receiptRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        ImportReceipt existing = findById(id);
        rollbackStock(existing.getItems());
        receiptRepository.delete(existing);
    }

    private void validateBasic(ImportReceiptRequest req, Long currentId) {
        if (req == null) throw new IllegalArgumentException("Dữ liệu phiếu nhập không hợp lệ.");
        String code = trim(req.getCode());
        if (code == null) throw new IllegalArgumentException("Mã phiếu nhập là bắt buộc.");
        if (currentId == null && receiptRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã phiếu nhập đã tồn tại.");
        }
        if (currentId != null) {
            receiptRepository.findByCode(code).ifPresent(found -> {
                if (!found.getId().equals(currentId)) {
                    throw new IllegalArgumentException("Mã phiếu nhập đã tồn tại.");
                }
            });
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một sản phẩm.");
        }
    }

    private void applyFields(ImportReceipt receipt, ImportReceiptRequest req) {
        receipt.setCode(trim(req.getCode()));
        receipt.setSupplierName(trim(req.getSupplierName()));
        receipt.setNote(trim(req.getNote()));
        receipt.setCreatedBy(trim(req.getCreatedBy()));
        String status = trim(req.getStatus());
        receipt.setStatus(status == null ? "completed" : status.toLowerCase(Locale.ROOT));
        if (receipt.getCreatedAt() == null) receipt.setCreatedAt(Instant.now());
        receipt.setUpdatedAt(Instant.now());
    }

    @SuppressWarnings("null")
    private void applyItemsAndStock(ImportReceipt receipt, ImportReceiptRequest req) {
        List<Long> ids = req.getItems().stream()
                .map(ImportReceiptRequest.Item::getProductId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Product> productMap = productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal total = BigDecimal.ZERO;
        List<ImportReceiptItem> built = new ArrayList<>();
        for (ImportReceiptRequest.Item itemReq : req.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            if (product == null) throw new IllegalArgumentException("Sản phẩm không tồn tại: " + itemReq.getProductId());

            int qty = itemReq.getQuantity() == null ? 0 : itemReq.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");

            BigDecimal unitCost = itemReq.getUnitCost() == null ? BigDecimal.ZERO : itemReq.getUnitCost();
            if (unitCost.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Đơn giá nhập không hợp lệ.");

            ImportReceiptItem item = new ImportReceiptItem();
            item.setReceipt(receipt);
            item.setProduct(product);
            item.setProductNameSnapshot(product.getName());
            item.setQuantity(qty);
            item.setUnitCost(unitCost);
            item.setLineTotal(unitCost.multiply(BigDecimal.valueOf(qty)));
            built.add(item);
            total = total.add(item.getLineTotal());

            int oldStock = product.getStockQty() == null ? 0 : product.getStockQty();
            product.setStockQty(oldStock + qty);
        }
        receipt.getItems().clear();
        receipt.getItems().addAll(built);
        receipt.setTotalCost(total);
        productRepository.saveAll(productMap.values());
    }

    private void rollbackStock(List<ImportReceiptItem> oldItems) {
        if (oldItems == null || oldItems.isEmpty()) return;
        for (ImportReceiptItem item : oldItems) {
            Product p = item.getProduct();
            int oldStock = p.getStockQty() == null ? 0 : p.getStockQty();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            int next = oldStock - qty;
            if (next < 0) {
                throw new IllegalArgumentException("Không thể sửa/xóa phiếu nhập vì tồn kho đã phát sinh giao dịch.");
            }
            p.setStockQty(next);
            productRepository.save(p);
        }
    }

    private static String trim(String s) {
        if (s == null) return null;
        String out = s.trim();
        return out.isEmpty() ? null : out;
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
