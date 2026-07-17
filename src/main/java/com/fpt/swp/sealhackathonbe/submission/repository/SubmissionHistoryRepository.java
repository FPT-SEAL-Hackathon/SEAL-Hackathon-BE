package com.fpt.swp.sealhackathonbe.submission.repository;

import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionHistoryRepository extends JpaRepository<SubmissionHistory, UUID> {
    List<SubmissionHistory> findBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);

    List<SubmissionHistory> findByTeamIdAndRoundIdOrderByVersionNumberDesc(UUID teamId, UUID roundId);

    Optional<SubmissionHistory> findFirstBySubmissionIdOrderByVersionNumberDesc(UUID submissionId);
}
