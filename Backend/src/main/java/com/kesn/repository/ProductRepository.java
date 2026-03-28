package com.kesn.repository;

import com.kesn.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    /** Admin: sản phẩm mới (id lớn hơn) lên đầu */
    List<Product> findAllByOrderByIdDesc();
}
