package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncStatus;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepositoryEntityRepository extends JpaRepository<SubmissionRepositoryEntity, UUID> {
    Optional<SubmissionRepositoryEntity> findBySubmission_SubmissionId(UUID submissionId);

    // Batch fetch cho danh sach submission (Organizer overview / list API) de tranh N+1.
    List<SubmissionRepositoryEntity> findBySubmission_SubmissionIdIn(Collection<UUID> submissionIds);

    /**
     * Chuyen trang thai sang RUNNING mot cach atomic de chong hai request resync
     * chay dong thoi tren cung mot submission (khong dung global lock).
     * Cho phep lay lai lock neu ban ghi da RUNNING qua lau (staleBefore) —
     * phong truong hop request truoc chet giua chung va khong tra lock.
     * Tra ve 0 row = dang co sync khac chay -> caller phai tra 409.
     */
    @Transactional
    @Modifying
    @Query("UPDATE SubmissionRepositoryEntity r SET r.lastSyncStatus = :running, r.updatedAt = :now "
            + "WHERE r.submission.submissionId = :submissionId "
            + "AND (r.lastSyncStatus <> :running OR r.updatedAt < :staleBefore)")
    int markSyncRunning(@Param("submissionId") UUID submissionId,
                        @Param("now") LocalDateTime now,
                        @Param("staleBefore") LocalDateTime staleBefore,
                        @Param("running") RepositorySyncStatus running);

    /**
     * Nha lock RUNNING ve FAILED khi luong resync gap loi bat ngo sau khi da giu lock,
     * de submission khong bi ket o RUNNING den het timeout.
     */
    @Transactional
    @Modifying
    @Query("UPDATE SubmissionRepositoryEntity r SET r.lastSyncStatus = :failed, r.errorCode = :errorCode, r.updatedAt = :now "
            + "WHERE r.submission.submissionId = :submissionId AND r.lastSyncStatus = :running")
    int failRunningSync(@Param("submissionId") UUID submissionId,
                        @Param("errorCode") String errorCode,
                        @Param("now") LocalDateTime now,
                        @Param("running") RepositorySyncStatus running,
                        @Param("failed") RepositorySyncStatus failed);
}
