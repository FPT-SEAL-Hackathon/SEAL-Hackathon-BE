package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    List<Disqualifications> findByTeamId(UUID teamId);

    List<Disqualifications> findBySubmissionId(UUID submissionId);

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
}
