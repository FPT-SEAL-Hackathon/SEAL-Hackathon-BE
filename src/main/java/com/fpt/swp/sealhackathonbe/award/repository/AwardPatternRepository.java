package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.AwardPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AwardPatternRepository extends JpaRepository<AwardPattern, UUID> {
    List<AwardPattern> findByCategoryCategoryIdAndIsActiveTrueOrderByRankPositionAsc(UUID categoryId);

    Optional<AwardPattern> findByCategoryCategoryIdAndRankPosition(UUID categoryId, Integer rankPosition);
}
