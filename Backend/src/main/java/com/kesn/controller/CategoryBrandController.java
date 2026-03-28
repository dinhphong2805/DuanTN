package com.kesn.controller;

import com.kesn.repository.BrandRepository;
import com.kesn.repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class CategoryBrandController {

    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;

    public CategoryBrandController(CategoryRepository categoryRepo, BrandRepository brandRepo) {
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
    }

    // Tương ứng với adminApi.get('/categories/names')
    @GetMapping("/categories/names")
    public List<String> getCategoryNames() {
        return categoryRepo.findAllNames();
    }

    // Tương ứng với adminApi.get('/brands/names')
    @GetMapping("/brands/names")
    public List<String> getBrandNames() {
        return brandRepo.findAllNames();
    }
}