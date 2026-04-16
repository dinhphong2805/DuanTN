package com.kesn.repository;

import com.kesn.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
    List<UserVoucher> findByUserIdAndIsUsedFalse(Long userId);
    Optional<UserVoucher> findByUserIdAndVoucherCode(Long userId, String voucherCode);
}
