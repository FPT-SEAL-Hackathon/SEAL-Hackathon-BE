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

    @Query("SELECT rj FROM RoundJudge rj JOIN FETCH rj.judge LEFT JOIN FETCH rj.assignedBy JOIN FETCH rj.round WHERE rj.round.roundId = :roundId")
    List<RoundJudge> findByRoundRoundId(@Param("roundId") UUID roundId);

    @Query("SELECT rj FROM RoundJudge rj JOIN FETCH rj.judge LEFT JOIN FETCH rj.assignedBy JOIN FETCH rj.round WHERE rj.round.roundId = :roundId AND rj.isActive = true")
    List<RoundJudge> findActiveByRoundRoundId(@Param("roundId") UUID roundId);

    @Query("SELECT rj.judge FROM RoundJudge rj WHERE rj.round.roundId = :roundId AND rj.isActive = true")
    List<User> findJudgesByRoundRoundId(@Param("roundId") UUID roundId);

    @Query("SELECT rj.round FROM RoundJudge rj WHERE rj.judge.userId = :judgeId AND rj.isActive = true")
    List<Round> findRoundsByJudgeJudgeId(@Param("judgeId") UUID judgeId);
  
    @Query("SELECT rj FROM RoundJudge rj WHERE rj.judge.userId = :userId AND rj.round.roundId = :roundId AND rj.isActive = true")
    Optional<RoundJudge> findByJudge_UserIdAndRound_RoundId(@Param("userId") UUID userId, @Param("roundId") UUID roundId);

    // Hard delete user: user còn là judge hoặc người phân công judge → chặn xóa.
    boolean existsByJudge_UserId(UUID userId);

    boolean existsByAssignedBy_UserId(UUID userId);

    // BR-19 (chiều ngược): user đã là judge active trong round của category → không được assign làm mentor.
    @Query("SELECT COUNT(rj) > 0 FROM RoundJudge rj WHERE rj.judge.userId = :judgeId AND rj.round.category.categoryId = :categoryId AND rj.isActive = true")
    boolean existsActiveJudgeInCategory(@Param("judgeId") UUID judgeId, @Param("categoryId") UUID categoryId);
}
