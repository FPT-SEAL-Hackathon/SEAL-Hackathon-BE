package com.fpt.swp.sealhackathonbe.judging.repository;

import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JudgingRepository extends JpaRepository<Judging, UUID> {

    List<Judging> findBySubmission_SubmissionId(UUID submissionId);

    List<Judging> findByRoundJudge_RoundJudgeId(UUID roundJudgeId);

    Optional<Judging> findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
            UUID submissionId, UUID roundJudgeId, UUID roundCriteriaId);

    List<Judging> findBySubmission_SubmissionIdIn(List<UUID> submissionIds);
}