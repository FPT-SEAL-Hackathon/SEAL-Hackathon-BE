package com.fpt.swp.sealhackathonbe.judging.repository;

import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JudgingRepository extends JpaRepository<Judging, UUID> {

    List<Judging> findBySubmissionId(UUID submissionId);

    List<Judging> findByRoundJudgeId(UUID roundJudgeId);

    Optional<Judging> findBySubmissionIdAndRoundJudgeIdAndRoundCriterionId(
            UUID submissionId, UUID judgeUserId, UUID eventCriterionId);
}