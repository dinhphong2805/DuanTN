// BrandRepository.java
package com.kesn.repository;
import com.kesn.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    @Query("SELECT b.name FROM Brand b ORDER BY b.name ASC")
    List<String> findAllNames();
}