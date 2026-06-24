package com.fpt.swp.sealhackathonbe.research.repository;

import com.fpt.swp.sealhackathonbe.research.entity.CalibrationSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CalibrationSampleRepository extends JpaRepository<CalibrationSample, UUID> {
    List<CalibrationSample> findByRound_RoundIdOrderByAddedAtDesc(UUID roundId);

    boolean existsByRound_RoundIdAndSubmission_SubmissionId(UUID roundId, UUID submissionId);
}
