package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    List<Disqualifications> findByTeamId(UUID teamId);

    List<Disqualifications> findBySubmissionId(UUID submissionId);

    @EntityGraph(attributePaths = {"disqualifiedBy"})
    Optional<Disqualifications> findTopByTeamIdAndReversedFalseOrderByDisqualifiedAtDesc(UUID teamId);

    @Query("""
            SELECT d FROM Disqualifications d
            WHERE d.submissionId = :submissionId AND d.reversed = false
            ORDER BY d.disqualifiedAt DESC
            """)
    List<Disqualifications> findActiveBySubmissionIdOrderByDisqualifiedAtDesc(
            @Param("submissionId") UUID submissionId
    );

    @Query("""
            SELECT d FROM Disqualifications d
            WHERE d.submissionId IN :submissionIds AND d.reversed = false
            ORDER BY d.disqualifiedAt DESC
            """)
    List<Disqualifications> findActiveBySubmissionIdInOrderByDisqualifiedAtDesc(
            @Param("submissionIds") List<UUID> submissionIds
    );

    @Query("""
            SELECT d FROM Disqualifications d
            WHERE d.submission.roundId = :roundId AND d.reversed = false
            ORDER BY d.disqualifiedAt DESC
            """)
    List<Disqualifications> findActiveSubmissionDisqualifications(
            @Param("roundId") UUID roundId
    );

    @Query("""
            SELECT d FROM Disqualifications d
            WHERE d.team.categoryId = :categoryId
              AND d.reversed = false
              AND EXISTS (
                  SELECT s FROM Submissions s
                  WHERE s.teamId = d.teamId AND s.roundId = :roundId
              )
            ORDER BY d.disqualifiedAt DESC
            """)
    List<Disqualifications> findActiveTeamDisqualifications(
            @Param("roundId") UUID roundId,
            @Param("categoryId") UUID categoryId
    );

    @Query("""
            SELECT d FROM Disqualifications d
            WHERE d.team.categoryId = :categoryId
              AND d.reversed = false
            """)
    List<Disqualifications> findActiveTeamDisqualificationsByCategory(
            @Param("categoryId") UUID categoryId
    );

    // Hard delete user: user còn là người ra/gỡ quyết định loại team → chặn xóa.
    boolean existsByDisqualifiedBy_UserId(UUID userId);

    boolean existsByReversedBy_UserId(UUID userId);
}
