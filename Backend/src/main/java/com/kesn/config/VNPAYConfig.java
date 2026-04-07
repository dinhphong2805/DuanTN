package com.kesn.config;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPAYConfig {
    // 1. URL thanh toán Sandbox
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // 2. URL nhận kết quả (Phải khớp với Route bên Vue của Nguyên)
    public static String vnp_ReturnUrl = "http://localhost:5173/payment-return"; 

    // 3. BỘ MÃ ĐỊNH DANH (QUAN TRỌNG: Sửa đúng cặp này để hết lỗi 72)
    public static String vnp_TmnCode = "2QN0Y4JI"; 
    public static String vnp_HashSecret = "MHCOHVRPRXWFOYUXZPHZAFXUKXNHLYXN"; 

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