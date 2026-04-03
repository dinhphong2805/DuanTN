package com.kesn.controller;

import com.kesn.dto.ChatTurnDto;
import com.kesn.dto.SupportChatRequest;
import com.kesn.dto.SupportChatResponse;
import com.kesn.service.GeminiSupportChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class SupportChatController {

    private final GeminiSupportChatService geminiSupportChatService;

    public SupportChatController(GeminiSupportChatService geminiSupportChatService) {
        this.geminiSupportChatService = geminiSupportChatService;
    }

    @GetMapping("/support/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "enabled", geminiSupportChatService.isConfigured()
        ));
    }

    @PostMapping("/support")
    public ResponseEntity<?> chat(@RequestBody SupportChatRequest req) {
        if (!geminiSupportChatService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message",
                            "Chat AI chưa được cấu hình. Đặt gemini.api.key trong application-local.properties "
                                    + "hoặc biến môi trường GEMINI_API_KEY (https://aistudio.google.com/apikey)."));
        }
        List<ChatTurnDto> messages = req != null ? req.getMessages() : null;
        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Vui lòng gửi ít nhất một tin nhắn."));
        }

        try {
            String reply = geminiSupportChatService.chat(messages);
            return ResponseEntity.ok(new SupportChatResponse(reply));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            String raw = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            if (raw.startsWith("GEMINI_HTTP_")) {
                String detail = raw.replaceFirst("^GEMINI_HTTP_\\d+:\\s*", "").trim();
                detail = userFacingGeminiMessage(detail);
                if (detail.length() > 320) {
                    detail = detail.substring(0, 317) + "…";
                }
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("message", detail.isEmpty()
                                ? "Lỗi từ Gemini API. Kiểm tra API key và model trong application-local.properties."
                                : detail));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", raw.length() > 280 ? raw.substring(0, 277) + "…" : raw));
        }
    }

    /** Rút gọn lỗi kỹ thuật Google thành thông báo dễ hiểu cho khách. */
    private static String userFacingGeminiMessage(String detail) {
        if (detail == null || detail.isBlank()) {
            return detail;
        }
        String lower = detail.toLowerCase(Locale.ROOT);
        if (lower.contains("quota") && lower.contains("exceed")
                || lower.contains("resource has been exhausted")
                || lower.contains("rate limit")
                || lower.contains("too many requests")) {
            return "Trợ lý AI đã hết hạn mức (quota) miễn phí theo ngày/phút của Google. "
                    + "Bạn thử lại sau vài giờ, hoặc liên hệ cửa hàng qua trang Liên hệ để được hỗ trợ trực tiếp. "
                    + "(Admin: xem https://ai.google.dev/gemini-api/docs/rate-limits và mục Usage trên Google AI Studio.)";
        }
        if (lower.contains("api key not valid") || lower.contains("invalid api key")) {
            return "API key Gemini không hợp lệ hoặc chưa bật API. Kiểm tra GEMINI_API_KEY hoặc gemini.api.key (file application-local.properties).";
        }
        return detail;
    }
}
