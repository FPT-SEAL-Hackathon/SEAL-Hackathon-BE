package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AwardRepository extends JpaRepository<Award, Long> {
    Optional<Award> findById(UUID id);
    List<Award> findByEventId(UUID eventId);
    List<Award> findByIsPublishedTrueOrderByAwardedAtDesc();
}
