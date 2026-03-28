package com.kesn.controller;

import com.kesn.dto.UserDto;
import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> list() {
        List<UserDto> dtos = userRepository.findAllByOrderByIdAsc().stream()
                .map(AdminUserController::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserDto> patchProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User u = opt.get();
        if (body.containsKey("fullName")) {
            String fn = body.get("fullName");
            u.setFullName(fn != null ? fn.trim() : null);
        }
        if (body.containsKey("phone")) {
            String p = body.get("phone");
            u.setPhone(p != null ? p.trim() : null);
        }
        return ResponseEntity.ok(toDto(userRepository.save(u)));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String r = role.trim().toLowerCase(Locale.ROOT);
        if (!r.equals("admin") && !r.equals("customer")) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User u = opt.get();
        if ("admin".equalsIgnoreCase(u.getRole()) && !"admin".equals(r)) {
            long admins = userRepository.countByRole("admin");
            if (admins <= 1) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Không thể đổi vai trò: đây là tài khoản admin duy nhất."));
            }
        }
        u.setRole(r);
        return ResponseEntity.ok(toDto(userRepository.save(u)));
    }

    private static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole());
    }
}
