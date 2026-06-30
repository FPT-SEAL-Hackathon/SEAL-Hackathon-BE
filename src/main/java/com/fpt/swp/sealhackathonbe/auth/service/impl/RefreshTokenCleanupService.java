package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Dọn dẹp định kỳ các refresh token đã hết hạn.
 */
@Service
public class RefreshTokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Nhận repository để xóa token hết hạn theo batch.
     */
    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Refresh Token:
    // Chỉ xóa token có thời điểm hết hạn nhỏ hơn hiện tại và ghi log số lượng.
    /**
     * Chạy theo cron cấu hình để giảm dữ liệu phiên không còn dùng được.
     */
    @Scheduled(cron = "${refresh-token.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void deleteExpiredRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        long deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(now);

        log.info("Deleted {} expired refresh token(s) before {}", deletedCount, now);
    }
}
