package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập dữ liệu phiên refresh token.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Tìm phiên theo giá trị refresh token đã lưu.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Liệt kê các phiên refresh token của một người dùng.
     */
    List<RefreshToken> findByUser_UserId(UUID userId);

    /**
     * Xóa phiên theo giá trị refresh token.
     */
    void deleteByTokenHash(String tokenHash);

    // Refresh Token:
    // Hỗ trợ scheduler xóa hàng loạt token đã hết hạn.
    /**
     * Chỉ xóa token có expiresAt nhỏ hơn mốc thời gian truyền vào.
     */
    long deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
