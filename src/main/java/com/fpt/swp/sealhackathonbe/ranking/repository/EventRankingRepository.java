package com.fpt.swp.sealhackathonbe.ranking.repository;

import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRankingRepository extends JpaRepository<EventRanking, UUID> {
    List<EventRanking> findByEventIdAndCategoryId(UUID eventId, UUID categoryId);
}
