package com.kesn.config;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VNPAYConfig {
    // 1. URL thanh toán Sandbox
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // 2. URL nhận kết quả (Phải khớp với Route bên Vue của bạn)
    public static String vnp_ReturnUrl = env("VNPAY_RETURN_URL", "http://localhost:5173/payment-return");
    
    // 3. Thông tin terminal Sandbox
    // Đã điền thông tin WV64SLHU và ADAAHH778O6BR46T0UNQ6HTHDMUZDMIZ vào giá trị mặc định (defaultValue)
    public static String vnp_TmnCode = env("VNPAY_TMN_CODE", "WV64SLHU");
    public static String vnp_HashSecret = env("VNPAY_HASH_SECRET", "ADAAHH778O6BR46T0UNQ6HTHDMUZDMIZ");

    // 4. Các thông số phiên bản
    public static String vnp_Version = "2.1.0";
    public static String vnp_Command = "pay";

    /**
     * Hàm băm dữ liệu HMAC SHA512 - Cực kỳ quan trọng để bảo mật
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) throw new NullPointerException();
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) return defaultValue;
        value = value.trim();
        return value.isEmpty() ? defaultValue : value;
    }

    /**
     * Hàm lấy IP người dùng - VNPAY dùng để chống gian lận
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Fix trường hợp IP local IPv6
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
}