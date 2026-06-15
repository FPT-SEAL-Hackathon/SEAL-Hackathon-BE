package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AwardRepository extends JpaRepository<Award, UUID> {
    List<Award> findAllByEventEventId(UUID eventId);
    List<Award> findByIsPublishedTrueOrderByAwardedAtDesc();
}
