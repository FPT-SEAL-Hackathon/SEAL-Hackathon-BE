package com.fpt.swp.sealhackathonbe.submission.repository;

import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionsRepository extends JpaRepository<Submissions, UUID> {
    Optional<Submissions> findByTeamIdAndRoundId(UUID teamId, UUID roundId);

    List<Submissions> findByRoundId(UUID roundId);

    boolean existsByTeamIdAndRoundId(UUID teamId, UUID roundId);

}
