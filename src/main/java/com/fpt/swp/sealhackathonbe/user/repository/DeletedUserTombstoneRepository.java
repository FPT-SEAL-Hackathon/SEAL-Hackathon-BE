package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.DeletedUserTombstone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Truy cập tombstone của tài khoản đã bị xóa cứng.
 */
@Repository
public interface DeletedUserTombstoneRepository extends JpaRepository<DeletedUserTombstone, UUID> {

    /**
     * Email này có tombstone còn hiệu lực không (phục vụ thông báo khi login).
     */
    boolean existsByEmailIgnoreCaseAndExpiresAtAfter(String email, LocalDateTime now);

    /**
     * Scheduler xóa các tombstone đã quá hạn lưu (7 ngày).
     */
    long deleteByExpiresAtBefore(LocalDateTime now);
}
