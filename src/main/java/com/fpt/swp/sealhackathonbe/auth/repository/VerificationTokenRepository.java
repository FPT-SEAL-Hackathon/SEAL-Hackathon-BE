package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.VerificationToken;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập dữ liệu token xác minh email.
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /**
     * Tìm token xác minh theo giá trị đã gửi qua email.
     */
    Optional<VerificationToken> findByTokenHash(String tokenHash);

    /**
     * Tìm các token chưa sử dụng của user để vô hiệu hóa khi cấp token mới.
     */
    List<VerificationToken> findByUserAndUsedAtIsNull(User user);
}
