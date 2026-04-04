package com.kesn.service;

import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import com.kesn.dto.GoogleOAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        String email = userInfo.getEmail() != null ? userInfo.getEmail().trim() : null;
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user", "Google không trả về email (cần scope email).", null));
        }

        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            // DB users.password thường NOT NULL; OAuth không có mật khẩu — lưu hash ngẫu nhiên (không đăng nhập bằng mật khẩu được)
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setFullName(userInfo.getName());
            newUser.setProvider("google");
            newUser.setProviderId(userInfo.getId());
            newUser.setImageUrl(userInfo.getImageUrl());
            newUser.setRole("customer");
            return userRepository.save(newUser);
        });
        return oAuth2User;
    }
}