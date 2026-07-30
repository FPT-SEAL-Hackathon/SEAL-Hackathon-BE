package com.fpt.swp.sealhackathonbe.submission.repository;

import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionHistoryRepository extends JpaRepository<SubmissionHistory, UUID> {
    // Lich su cua mot submission cu the, version moi nhat dung dau.
    List<SubmissionHistory> findBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    // Member xem lich su submission cua team trong mot round theo unique team-round.
    List<SubmissionHistory> findByTeamIdAndRoundIdOrderByVersionNumberDesc(UUID teamId, UUID roundId);

    // Tim version moi nhat de tinh versionNumber tiep theo khi ghi snapshot.
    Optional<SubmissionHistory> findFirstBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    // Round service dung de chan xoa round khi da co lich su submission.
    boolean existsByRoundId(UUID roundId);
}
