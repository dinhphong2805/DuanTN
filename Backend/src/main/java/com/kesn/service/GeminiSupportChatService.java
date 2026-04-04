package com.kesn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kesn.dto.ChatTurnDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GeminiSupportChatService {

    private static final Logger log = LoggerFactory.getLogger(GeminiSupportChatService.class);

    /** Dự phòng nếu model chính 404 / not found hoặc không khả dụng với key hiện tại. */
    private static final String[] MODEL_FALLBACKS = {
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash-preview-09-2025",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-flash-latest",
    };

    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý hỗ trợ trực tuyến của cửa hàng giày Kesn Store (website thương mại điện tử).
            Trả lời ngắn gọn, thân thiện, bằng tiếng Việt.
            Bạn không truy cập được cơ sở dữ liệu đơn hàng hay tài khoản: nếu khách hỏi trạng thái đơn cụ thể, mật khẩu, hoặc thông tin riêng, hãy hướng dẫn họ đăng nhập vào website, xem mục đơn hàng / hồ sơ, hoặc liên hệ cửa hàng qua trang Liên hệ.
            Không bịa mẫu sản phẩm, giá, hoặc chính sách không chắc chắn. Nếu không biết, nói rõ và gợi ý xem FAQ hoặc liên hệ hỗ trợ.
            """;

    private static final int MAX_TURNS = 24;
    private static final int MAX_TEXT_LEN = 4000;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public GeminiSupportChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return effectiveKey() != null;
    }

    private String effectiveKey() {
        if (apiKey == null) {
            return null;
        }
        String k = apiKey.trim();
        return k.isEmpty() ? null : k;
    }

    public String chat(List<ChatTurnDto> messages) throws Exception {
        String key = effectiveKey();
        if (key == null) {
            throw new IllegalStateException("GEMINI_NOT_CONFIGURED");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống.");
        }

        List<ChatTurnDto> trimmed = trimMessages(messages);

        List<Map<String, Object>> contents = new ArrayList<>();
        boolean systemInjected = false;
        for (ChatTurnDto t : trimmed) {
            String role = normalizeRole(t.getRole());
            String text = clamp(t.getText());
            if (text.isBlank()) {
                continue;
            }
            if (!systemInjected && "user".equals(role)) {
                text = SYSTEM_PROMPT + "\n\n---\n\n" + text;
                systemInjected = true;
            }
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", text))
            ));
        }
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Không có nội dung hợp lệ.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", contents);
        body.put("generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 1024
        ));

        String json = objectMapper.writeValueAsString(body);
        String primary = (model != null ? model.trim() : "");
        if (primary.isEmpty()) {
            primary = MODEL_FALLBACKS[0];
        }

        Exception last = null;
        for (String tryModel : orderedModels(primary)) {
            try {
                return callGenerateContent(key, tryModel, json);
            } catch (Exception e) {
                last = e;
                if (!isRetryableModelError(e)) {
                    throw e;
                }
                log.warn("Gemini model {} failed, trying next: {}", tryModel, e.getMessage());
            }
        }
        if (last != null) {
            throw last;
        }
        throw new IllegalStateException("Không có model Gemini khả dụng.");
    }

    private List<String> orderedModels(String primary) {
        List<String> out = new ArrayList<>();
        out.add(primary);
        for (String m : MODEL_FALLBACKS) {
            if (!out.contains(m)) {
                out.add(m);
            }
        }
        return out;
    }

    private static boolean isRetryableModelError(Exception e) {
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        if (m.contains("GEMINI_HTTP_401") || m.contains("GEMINI_HTTP_403")) {
            return false;
        }
        if (m.contains("GEMINI_HTTP_404")) {
            return true;
        }
        String lower = m.toLowerCase(Locale.ROOT);
        if (m.contains("GEMINI_HTTP_400") && (lower.contains("not found") || lower.contains("is not found"))) {
            return true;
        }
        return lower.contains("is not found") || lower.contains("not supported for generatecontent");
    }

    private String callGenerateContent(String key, String modelName, String jsonBody) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelName + ":generateContent?key=" + key;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body();
            String msg = "HTTP " + response.statusCode();
            try {
                JsonNode errRoot = objectMapper.readTree(body);
                msg = errRoot.path("error").path("message").asText(msg);
            } catch (Exception ignored) {
                if (body != null && body.length() < 500) {
                    msg = msg + ": " + body;
                }
            }
            throw new IllegalStateException("GEMINI_HTTP_" + response.statusCode() + ": " + msg);
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            String block = root.path("promptFeedback").path("blockReason").asText("");
            if (!block.isBlank()) {
                throw new IllegalStateException("Nội dung bị từ chối bởi chính sách an toàn (" + block + ").");
            }
            throw new IllegalStateException("Gemini không trả về câu trả lời.");
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new IllegalStateException("Phản hồi từ AI không có nội dung.");
        }

        StringBuilder reply = new StringBuilder();
        for (JsonNode p : parts) {
            reply.append(p.path("text").asText(""));
        }
        String out = reply.toString().trim();
        if (out.isEmpty()) {
            throw new IllegalStateException("Phản hồi từ AI rỗng.");
        }
        log.info("Gemini OK model={}", modelName);
        return out;
    }

    private static List<ChatTurnDto> trimMessages(List<ChatTurnDto> messages) {
        int from = Math.max(0, messages.size() - MAX_TURNS);
        return new ArrayList<>(messages.subList(from, messages.size()));
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "user";
        }
        String r = role.trim().toLowerCase(Locale.ROOT);
        if ("model".equals(r) || "assistant".equals(r)) {
            return "model";
        }
        return "user";
    }

    private static String clamp(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.length() > MAX_TEXT_LEN) {
            return t.substring(0, MAX_TEXT_LEN);
        }
        return t;
    }
}
