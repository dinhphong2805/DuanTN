package com.kesn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kesn.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class AdminAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        return !path.startsWith("/api/admin");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "Chưa đăng nhập");
            return;
        }

        String token = authHeader.substring(7).trim();
        Long userId = parseUserId(token);
        if (userId == null) {
            sendError(response, 401, "Token không hợp lệ");
            return;
        }

        boolean isAdmin = userRepository.findById(userId)
                .map(u -> "admin".equalsIgnoreCase(u.getRole()) || "staff".equalsIgnoreCase(u.getRole()))
                .orElse(false);

        if (!isAdmin) {
            sendError(response, 403, "Bạn không có quyền truy cập trang admin");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Long parseUserId(String token) {
        if (token == null || !token.startsWith("jwt-token-")) return null;
        try {
            return Long.parseLong(token.substring(10));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", message)));
    }
}
