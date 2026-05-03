package com.kesn.repository;

import com.kesn.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    /** Lấy banner đang active, sắp xếp theo sort_order tăng dần */
    List<Banner> findByActiveTrueOrderBySortOrderAsc();

    /** Lấy tất cả banner theo sort_order */
    List<Banner> findAllByOrderBySortOrderAsc();
}
