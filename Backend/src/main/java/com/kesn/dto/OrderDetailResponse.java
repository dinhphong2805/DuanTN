package com.kesn.dto;

import com.kesn.entity.Order;
import com.kesn.entity.OrderItem;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** JSON chi tiết đơn — tách khỏi entity để tránh lỗi serialize Hibernate/Jackson. */
public class OrderDetailResponse {

    public Long id;
    public Long userId;
    public String customerName;
    public String customerEmail;
    public String customerPhone;
    public String address;
    public String status;
    public BigDecimal total;
    /** ISO-8601 string */
    public String createdAt;
    public List<ItemRow> items;

    public static class ItemRow {
        public String productName;
        public Integer quantity;
        public BigDecimal unitPrice;
    }

    public static OrderDetailResponse fromEntity(Order o) {
        OrderDetailResponse r = new OrderDetailResponse();
        r.id = o.getId();
        r.userId = o.getUserId();
        r.customerName = o.getCustomerName();
        r.customerEmail = o.getCustomerEmail();
        r.customerPhone = o.getCustomerPhone();
        r.address = o.getAddress();
        r.status = o.getStatus();
        r.total = o.getTotal();
        r.createdAt = o.getCreatedAt() != null ? o.getCreatedAt().toString() : null;
        if (o.getItems() != null && !o.getItems().isEmpty()) {
            r.items = o.getItems().stream().map(OrderDetailResponse::mapItem).collect(Collectors.toList());
        } else {
            r.items = Collections.emptyList();
        }
        return r;
    }

    private static ItemRow mapItem(OrderItem i) {
        ItemRow row = new ItemRow();
        row.productName = i.getProductName();
        row.quantity = i.getQuantity();
        row.unitPrice = i.getUnitPrice();
        return row;
    }
}
