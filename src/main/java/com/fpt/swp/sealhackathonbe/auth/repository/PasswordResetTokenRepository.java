package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.PasswordResetToken;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập token reset mật khẩu (chỉ lưu hash, dùng một lần).
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    List<PasswordResetToken> findByUserAndUsedAtIsNull(User user);
}
