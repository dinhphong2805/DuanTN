package com.kesn.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vouchers", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, precision = 19, scale = 0)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String type = "percent"; // percent | fixed

    @Column(name = "min_order", precision = 19, scale = 0)
    private BigDecimal minOrder = BigDecimal.ZERO;

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
}
