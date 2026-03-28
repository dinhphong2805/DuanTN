package com.kesn.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Map tới bảng Vouchers của SQL Server (schema chuẩn: Value, MinOrderAmount, …).
 */
@Entity
@Table(name = "Vouchers", uniqueConstraints = @UniqueConstraint(columnNames = "Code"))
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Code", nullable = false, length = 50)
    private String code;

    /** Cột DB: Value (giá trị % hoặc số tiền cố định) */
    @Column(name = "Value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "Type", nullable = false, length = 20)
    private String type = "percent";

    @Column(name = "MinOrderAmount", precision = 18, scale = 2)
    private BigDecimal minOrder = BigDecimal.ZERO;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "StartAt", nullable = false)
    private Instant startAt;

    @Column(name = "EndAt", nullable = false)
    private Instant endAt;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "active";

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (startAt == null) startAt = now;
        if (endAt == null) endAt = now.plusSeconds(10L * 365 * 24 * 3600);
        if (status == null || status.isBlank()) status = "active";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getMinOrder() { return minOrder; }
    public void setMinOrder(BigDecimal minOrder) { this.minOrder = minOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }
    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
