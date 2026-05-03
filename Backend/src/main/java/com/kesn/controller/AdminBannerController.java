package com.kesn.controller;

import com.kesn.entity.Banner;
import com.kesn.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Banner Controller — CRUD quản lý banner trang chủ
 * Endpoint: /api/admin/banners  (yêu cầu JWT admin)
 */
@RestController
@RequestMapping("/api/admin/banners")
@CrossOrigin(origins = {
    "http://localhost:5173", "http://localhost:5174",
    "http://127.0.0.1:5173", "http://127.0.0.1:5174"
})
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    public AdminBannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /** GET /api/admin/banners — danh sách tất cả banner */
    @GetMapping
    public ResponseEntity<List<Banner>> list() {
        return ResponseEntity.ok(bannerRepository.findAllByOrderBySortOrderAsc());
    }

    /** POST /api/admin/banners — tạo banner mới */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String imageUrl = (String) body.get("imageUrl");
        String title    = (String) body.getOrDefault("title", "Banner");
        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "imageUrl không được để trống"));
        }
        Banner b = new Banner();
        b.setTitle(title.trim());
        b.setImageUrl(imageUrl.trim());
        b.setLinkUrl(nullOrTrim(body.get("linkUrl")));
        b.setSortOrder(toInt(body.get("sortOrder"), 0));
        b.setActive(toBool(body.get("active"), true));
        return ResponseEntity.ok(bannerRepository.save(b));
    }

    /** PUT /api/admin/banners/{id} — cập nhật banner */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return bannerRepository.findById(id).map(b -> {
            if (body.containsKey("title"))     b.setTitle(String.valueOf(body.get("title")).trim());
            if (body.containsKey("imageUrl"))  b.setImageUrl(String.valueOf(body.get("imageUrl")).trim());
            if (body.containsKey("linkUrl"))   b.setLinkUrl(nullOrTrim(body.get("linkUrl")));
            if (body.containsKey("sortOrder")) b.setSortOrder(toInt(body.get("sortOrder"), b.getSortOrder()));
            if (body.containsKey("active"))    b.setActive(toBool(body.get("active"), b.getActive()));
            return ResponseEntity.ok(bannerRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** PATCH /api/admin/banners/{id}/toggle — bật/tắt active */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        return bannerRepository.findById(id).map(b -> {
            b.setActive(!b.getActive());
            return ResponseEntity.ok(bannerRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/admin/banners/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!bannerRepository.existsById(id)) return ResponseEntity.notFound().build();
        bannerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ---- helpers ----
    private static String nullOrTrim(Object o) {
        if (o == null || o.toString().isBlank()) return null;
        return o.toString().trim();
    }
    private static int toInt(Object o, int fallback) {
        if (o == null) return fallback;
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return fallback; }
    }
    private static boolean toBool(Object o, boolean fallback) {
        if (o == null) return fallback;
        return Boolean.parseBoolean(o.toString());
    }
}
