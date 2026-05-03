package com.kesn.controller;

import com.kesn.entity.Banner;
import com.kesn.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Banner API — không cần đăng nhập
 * Endpoint: /api/banners  → chỉ trả về banner active
 */
@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = {
    "http://localhost:5173", "http://localhost:5174",
    "http://127.0.0.1:5173", "http://127.0.0.1:5174"
})
public class BannerController {

    private final BannerRepository bannerRepository;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /** GET /api/banners — chỉ lấy banner đang active để hiển thị trang chủ */
    @GetMapping
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerRepository.findByActiveTrueOrderBySortOrderAsc());
    }
}
