package com.kesn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response từ GHN Shipping Fee API
 */
public class ShippingFeeResponse {
    
    @JsonProperty("code")
    private Integer code; // 200 = success
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private ShippingData data;

    public static class ShippingData {
        @JsonProperty("total")
        private Long total; // Phí ship (VNĐ)
        
        @JsonProperty("service_fee")
        private Long serviceFee;
        
        @JsonProperty("insurance_fee")
        private Long insuranceFee;
        
        @JsonProperty("cod_failed_fee")
        private Long codFailedFee;
        
        @JsonProperty("estimated_pick_time")
        private Long estimatedPickTime;
        
        @JsonProperty("estimated_deliver_time")
        private Long estimatedDeliverTime;

        // Getters & Setters
        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        
        public Long getServiceFee() { return serviceFee; }
        public void setServiceFee(Long serviceFee) { this.serviceFee = serviceFee; }
        
        public Long getInsuranceFee() { return insuranceFee; }
        public void setInsuranceFee(Long insuranceFee) { this.insuranceFee = insuranceFee; }
        
        public Long getCodFailedFee() { return codFailedFee; }
        public void setCodFailedFee(Long codFailedFee) { this.codFailedFee = codFailedFee; }
        
        public Long getEstimatedPickTime() { return estimatedPickTime; }
        public void setEstimatedPickTime(Long estimatedPickTime) { this.estimatedPickTime = estimatedPickTime; }
        
        public Long getEstimatedDeliverTime() { return estimatedDeliverTime; }
        public void setEstimatedDeliverTime(Long estimatedDeliverTime) { this.estimatedDeliverTime = estimatedDeliverTime; }
    }

    // Getters & Setters
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public ShippingData getData() { return data; }
    public void setData(ShippingData data) { this.data = data; }
}
