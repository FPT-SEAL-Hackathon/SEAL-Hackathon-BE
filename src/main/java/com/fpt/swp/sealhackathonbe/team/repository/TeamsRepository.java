package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamsRepository extends JpaRepository<Teams, UUID> {
    // Kiểm tra trùng tên team trong cùng event trước khi tạo team mới.
    boolean existsByEventIdAndTeamName(UUID eventId, String teamName);

    List<Teams> findByEventId(UUID eventId);
    List<Teams> findByLeaderUserId(UUID leaderUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select team from Teams team where team.teamId = :teamId")
    Optional<Teams> findByIdForUpdate(@Param("teamId") UUID teamId);

    // Lấy tất cả teams trong một category (dùng cho mentor dashboard)
    List<Teams> findByCategoryId(UUID categoryId);

    // Đếm số teams trong một category
    long countByCategoryId(UUID categoryId);

    long countByEventId(UUID eventId);
}
