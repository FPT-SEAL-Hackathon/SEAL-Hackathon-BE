package com.fpt.swp.sealhackathonbe.criteria.repository;

import com.fpt.swp.sealhackathonbe.criteria.entity.EventCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventCriterionRepository extends JpaRepository<EventCriteria, UUID> {
    List<EventCriteria> findByEventId(UUID eventId);
}
