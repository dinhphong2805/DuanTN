package com.kesn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";
    private static final String[] ALLOWED = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chưa chọn file"));
        }
        String contentType = file.getContentType();
        if (contentType == null || (!java.util.Arrays.asList(ALLOWED).contains(contentType) && !contentType.startsWith("image/"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận ảnh: JPG, PNG, GIF, WebP (nhận: " + contentType + ")"));
        }
        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            String ext = contentType.split("/")[1].split(";")[0].trim();
            if (ext.isEmpty() || ext.length() > 10) ext = "png";
            String filename = UUID.randomUUID() + "." + ext;
            Path dest = dir.resolve(filename);
            file.transferTo(dest);
            String url = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Không thể lưu file: " + e.getMessage()));
        }
    }
}
