package com.kesn.config;

import com.kesn.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminAuthFilter adminAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    // Inject thêm 2 service mới vào constructor
    public SecurityConfig(AdminAuthFilter adminAuthFilter, 
                          CustomOAuth2UserService customOAuth2UserService, 
                          OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler) {
        this.adminAuthFilter = adminAuthFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
    }

    /**
     * Cho phép mọi cổng trên localhost / 127.0.0.1 (Vite đổi port) và IP LAN (nhóm test cùng mạng).
     * Không dùng danh sách origin cố định để tránh chỉ chạy được trên một máy/cổng.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*",
                "http://10.*:*"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // Quan trọng cho OAuth2
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                // Filter kiểm tra admin của bạn vẫn giữ nguyên
                .addFilterBefore(adminAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép các endpoint liên quan đến auth và oauth2 mà không cần login
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()
                        .anyRequest().permitAll() // Bạn đang để permitAll cho dự án, sau này nên siết lại
                )
                // --- CẤU HÌNH GOOGLE LOGIN TẠI ĐÂY ---
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // Xử lý lấy thông tin user từ Google
                        )
                        .successHandler(oauth2AuthenticationSuccessHandler) // Xử lý sau khi đăng nhập thành công (tạo token)
                );
        
        return http.build();
    }
}