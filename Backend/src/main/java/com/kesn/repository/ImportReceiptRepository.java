package com.kesn.repository;

import com.kesn.entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
    List<ImportReceipt> findAllByOrderByCreatedAtDesc();
    Optional<ImportReceipt> findByCode(String code);
    boolean existsByCode(String code);
}
