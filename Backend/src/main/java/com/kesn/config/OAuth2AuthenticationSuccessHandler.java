package com.kesn.config;

import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Object emailAttr = oAuth2User.getAttribute("email");
        String email = emailAttr != null ? emailAttr.toString().trim() : null;
        if (email == null || email.isEmpty()) {
            getRedirectStrategy().sendRedirect(request, response,
                    "http://localhost:5173/login?oauthError=no_email");
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            getRedirectStrategy().sendRedirect(request, response,
                    "http://localhost:5173/login?oauthError=user_not_found");
            return;
        }

        // Tạo Token 
        String token = "jwt-token-" + user.getId();
        
        // Lấy fullName xử lý tránh null
        String fullName = user.getFullName() != null ? user.getFullName() : "";

        // Sử dụng UTF8 để tránh bị lỗi ký tự
        String encodedName = URLEncoder.encode(fullName, StandardCharsets.UTF_8.toString());

        // Xây dựng URL Redirect về Frontend
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("userId", user.getId())
                .queryParam("email", user.getEmail())
                .queryParam("fullName", encodedName)
                .queryParam("role", user.getRole())
                .build()
                .toUriString();

      
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}