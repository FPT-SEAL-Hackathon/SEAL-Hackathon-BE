package com.fpt.swp.sealhackathonbe.round.repository;

import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundCriterionRepository extends JpaRepository<RoundCriterion, UUID> {
    List<RoundCriterion> findByRoundRoundIdOrderBySortOrderAsc(UUID roundId);
}
