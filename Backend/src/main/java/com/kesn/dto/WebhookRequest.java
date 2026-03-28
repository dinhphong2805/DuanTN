
package com.kesn.dto;

import java.math.BigDecimal;

public class WebhookRequest {
    private BigDecimal transferAmount; // Số tiền giao dịch
    private String content;            // Nội dung chuyển khoản
    private String transferType;       // Loại giao dịch ("in" là tiền vào, "out" là tiền ra)

    // Getters and Setters
    public BigDecimal getTransferAmount() { return transferAmount; }
    public void setTransferAmount(BigDecimal transferAmount) { this.transferAmount = transferAmount; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }
}