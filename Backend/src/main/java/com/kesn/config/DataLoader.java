package com.kesn.config;

import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@kesn.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@kesn.com");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setFullName("Admin Kesn");
            admin.setPhone("0901234567");
            admin.setRole("admin");
            userRepository.save(admin);
            System.out.println(">>> Đã tạo admin: admin@kesn.com / 123");
        }
    }
}
