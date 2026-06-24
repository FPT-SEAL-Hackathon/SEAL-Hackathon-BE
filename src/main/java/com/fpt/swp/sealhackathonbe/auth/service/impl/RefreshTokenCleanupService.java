package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenCleanupService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "${refresh-token.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void deleteExpiredRefreshTokens() {
        LocalDateTime now = LocalDateTime.now();
        long deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(now);

        log.info("Deleted {} expired refresh token(s) before {}", deletedCount, now);
    }
}
