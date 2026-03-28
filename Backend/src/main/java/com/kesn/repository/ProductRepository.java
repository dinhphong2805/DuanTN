package com.kesn.repository;

import com.kesn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm cái này
import java.util.List;

// Kế thừa thêm JpaSpecificationExecutor để chạy được bộ lọc động
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByNameContainingIgnoreCase(String name);
}