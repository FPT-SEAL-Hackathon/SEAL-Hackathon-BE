package com.fpt.swp.sealhackathonbe.judging.repository;

import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JudgingRepository extends JpaRepository<Judging, UUID> {

    List<Judging> findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(UUID submissionId, UUID UserId);

    List<Judging> findByRoundJudge_Judge_UserId(UUID roundJudgeId);

    Optional<Judging> findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
            UUID submissionId, UUID roundJudgeId, UUID roundCriteriaId);

    List<Judging> findBySubmission_SubmissionIdIn(List<UUID> submissionIds);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Judging j SET j.isActive = false WHERE j.roundJudge.roundJudgeId = :roundJudgeId")
    void disableByRoundJudge_RoundJudgeId(@org.springframework.data.repository.query.Param("roundJudgeId") UUID roundJudgeId);
    boolean existsByRoundJudge_RoundJudgeId(UUID roundJudgeId);
}