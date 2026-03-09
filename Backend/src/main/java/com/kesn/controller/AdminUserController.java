package com.kesn.controller;

import com.kesn.dto.UserDto;
import com.kesn.entity.User;
import com.kesn.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        List<UserDto> dtos = userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
