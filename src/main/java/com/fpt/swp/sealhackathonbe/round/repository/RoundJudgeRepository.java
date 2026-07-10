package com.fpt.swp.sealhackathonbe.round.repository;

import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundJudgeRepository extends JpaRepository<RoundJudge, UUID> {
    @Query("SELECT COUNT(rj) > 0 FROM RoundJudge rj WHERE rj.round.roundId = :roundId AND rj.isActive = true")
    boolean existsByRoundRoundId(@Param("roundId") UUID roundId);

    List<RoundJudge> findByRoundRoundId(UUID roundId);

    @Query("SELECT rj FROM RoundJudge rj WHERE rj.round.roundId = :roundId AND rj.isActive = true")
    List<RoundJudge> findActiveByRoundRoundId(@Param("roundId") UUID roundId);

    @Query("SELECT rj.judge FROM RoundJudge rj WHERE rj.round.roundId = :roundId AND rj.isActive = true")
    List<User> findJudgesByRoundRoundId(@Param("roundId") UUID roundId);

    @Query("SELECT rj.round FROM RoundJudge rj WHERE rj.judge.userId = :judgeId AND rj.isActive = true")
    List<Round> findRoundsByJudgeJudgeId(@Param("judgeId") UUID judgeId);
  
    @Query("SELECT rj FROM RoundJudge rj WHERE rj.judge.userId = :userId AND rj.round.roundId = :roundId AND rj.isActive = true")
    Optional<RoundJudge> findByJudge_UserIdAndRound_RoundId(@Param("userId") UUID userId, @Param("roundId") UUID roundId);
}
