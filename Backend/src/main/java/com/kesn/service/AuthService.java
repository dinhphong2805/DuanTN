package com.kesn.service;

import com.kesn.dto.*;
import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Random RANDOM = new Random();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }
        LoginResponse.UserDto dto = new LoginResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole()
        );
        return new LoginResponse(dto, "jwt-token-" + user.getId());
    }

    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName() != null && !req.getFullName().isBlank()
                ? req.getFullName() : req.getEmail().split("@")[0]);
        user.setPhone(req.getPhone());
        user.setRole("customer");
        user = userRepository.save(user);
        LoginResponse.UserDto dto = new LoginResponse.UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole()
        );
        return new LoginResponse(dto, "jwt-token-" + user.getId());
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email chưa được đăng ký"));
        String code = String.format("%06d", RANDOM.nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiresAt(Instant.now().plusSeconds(15 * 60)); // 15 phút
        userRepository.save(user);
        // TODO: Gửi email. Hiện tại log ra console để test
        System.out.println(">>> Reset code cho " + req.getEmail() + ": " + code);
        return new MessageResponse("Mã xác nhận đã gửi tới email của bạn. Kiểm tra hộp thư (hoặc console backend để test).");
    }

    public MessageResponse resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (user.getResetCode() == null || user.getResetCodeExpiresAt() == null) {
            throw new RuntimeException("Mã xác nhận không hợp lệ hoặc đã hết hạn");
        }
        if (!user.getResetCode().equals(req.getCode())) {
            throw new RuntimeException("Mã xác nhận không đúng");
        }
        if (Instant.now().isAfter(user.getResetCodeExpiresAt())) {
            user.setResetCode(null);
            user.setResetCodeExpiresAt(null);
            userRepository.save(user);
            throw new RuntimeException("Mã xác nhận đã hết hạn, vui lòng thử lại");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);
        return new MessageResponse("Đặt lại mật khẩu thành công");
    }
}
