package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.user.repository.DeletedUserTombstoneRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Dọn dẹp định kỳ tombstone của tài khoản đã xóa cứng sau thời gian lưu 7 ngày.
 */
@Service
public class DeletedUserTombstoneCleanupService {

    private static final Logger log = LoggerFactory.getLogger(DeletedUserTombstoneCleanupService.class);

    private final DeletedUserTombstoneRepository tombstoneRepository;

    public DeletedUserTombstoneCleanupService(DeletedUserTombstoneRepository tombstoneRepository) {
        this.tombstoneRepository = tombstoneRepository;
    }

    /**
     * Chỉ xóa tombstone có ExpiresAt nhỏ hơn hiện tại và ghi log số lượng.
     */
    @Scheduled(cron = "${user.tombstone.cleanup.cron:0 30 2 * * *}")
    @Transactional
    public void deleteExpiredTombstones() {
        LocalDateTime now = LocalDateTime.now();
        long deletedCount = tombstoneRepository.deleteByExpiresAtBefore(now);

        log.info("Deleted {} expired deleted-user tombstone(s) before {}", deletedCount, now);
    }
}
