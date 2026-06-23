package com.fpt.swp.sealhackathonbe.ranking.repository;

import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundRankingRepository extends JpaRepository<RoundRanking, UUID> {
    List<RoundRanking> findByRound_RoundIdAndCategory_CategoryId(UUID roundId, UUID categoryId);
    List<RoundRanking> findByRoundRoundIdAndTeamTeamIdIn(UUID roundId, List<UUID> teamIds);
    List<RoundRanking> findByRoundRoundIdAndCategoryCategoryIdOrderByRankPositionAsc(UUID roundId, UUID categoryId, Pageable pageable);
}
