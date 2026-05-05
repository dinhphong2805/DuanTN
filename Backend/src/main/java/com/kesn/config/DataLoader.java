package com.kesn.config;

import com.kesn.entity.Brand;
import com.kesn.entity.Category;
import com.kesn.entity.User;
import com.kesn.repository.BrandRepository;
import com.kesn.repository.CategoryRepository;
import com.kesn.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class DataLoader implements CommandLineRunner {

   
    private static final String[] SEED_BRANDS = {
            "Nike",
            "Adidas",
            "Stussy",
            "Cross",
            "Balenciaga",
    };

    
    private static final String[] SEED_CATEGORIES = {
            "Basketball",
            "Đá banh",
            "Bóng bầu dục",
            "Gym",
            "Các loại khác",
    };

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(
            UserRepository userRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedBrands();
        seedCategories();
    }

    private void seedAdmin() {
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

    private void seedBrands() {
        for (String name : SEED_BRANDS) {
            String slug = slugify(name, "brand");
            if (brandRepository.findBySlug(slug).isEmpty()) {
                Brand b = new Brand();
                b.setName(name);
                b.setSlug(slug);
                brandRepository.save(b);
                System.out.println(">>> Đã tạo thương hiệu: " + name);
            }
        }
    }

    private void seedCategories() {
        for (String name : SEED_CATEGORIES) {
            String slug = slugify(name, "category");
            if (categoryRepository.findBySlug(slug).isEmpty()) {
                Category c = new Category();
                c.setName(name);
                c.setSlug(slug);
                c.setParentId(null);
                categoryRepository.save(c);
                System.out.println(">>> Đã tạo danh mục: " + name);
            }
        }
    }

    private static String slugify(String input, String emptyFallback) {
        if (input == null || input.isBlank()) {
            return emptyFallback;
        }
        String s = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        s = s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return s.isEmpty() ? emptyFallback : s;
    }
}
