package com.kesn.controller;

import com.kesn.entity.Category;
import com.kesn.entity.Product;
import com.kesn.repository.CategoryRepository;
import com.kesn.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:5174"})
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên danh mục không được trống"));
        }
        category.setSlug(generateSlug(category.getName()));
        // Ensure slug is unique
        if (categoryRepository.findBySlug(category.getSlug()).isPresent()) {
            category.setSlug(category.getSlug() + "-" + System.currentTimeMillis());
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id).map(category -> {
            if (categoryDetails.getName() == null || categoryDetails.getName().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tên danh mục không được trống"));
            }
            category.setName(categoryDetails.getName());
            category.setSlug(generateSlug(category.getName()));
            if (categoryDetails.getParentId() != null) {
                category.setParentId(categoryDetails.getParentId());
            }
            return ResponseEntity.ok(categoryRepository.save(category));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        return categoryRepository.findById(id).map(category -> {
            // Check if any product is using this category (we check by name for now, later switch to ID if fully migrated)
            List<Product> usingProducts = productRepository.findByCategory(category.getName());
            if (!usingProducts.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không thể xóa danh mục đang có sản phẩm."));
            }
            categoryRepository.delete(category);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private String generateSlug(String input) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
        return slug.toLowerCase();
    }
}
