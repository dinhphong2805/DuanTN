package com.kesn.service;

import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import com.kesn.dto.GoogleOAuth2UserInfo;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        
        // Kiểm tra xem khách đã có tài khoản chưa, chưa có thì tạo mới
        userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(userInfo.getEmail());
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