package com.fpt.swp.sealhackathonbe.round.repository;

import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoundJudgeRepository extends JpaRepository<RoundJudge, UUID> {
    boolean existsByRoundRoundId(UUID roundId);

    @Query("SELECT rj.judge FROM RoundJudge rj WHERE rj.round.roundId = :roundId")
    List<User> findJudgesByRoundRoundId(@Param("roundId") UUID roundId);
}
