package com.kesn.repository;
import com.kesn.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query("SELECT c.name FROM Category c ORDER BY c.name ASC")
    List<String> findAllNames();
}