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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select team
            from Teams team
            where team.eventId = :eventId
              and lower(team.teamName) = lower(:teamName)
            """)
    List<Teams> findByEventIdAndTeamNameIgnoreCaseForUpdate(
            @Param("eventId") UUID eventId,
            @Param("teamName") String teamName
    );

    List<Teams> findByEventId(UUID eventId);

    @Query("""
            select distinct team
            from Teams team
            join TeamMembers member on member.teamId = team.teamId
            where team.eventId = :eventId
              and member.active = true
            """)
    List<Teams> findByEventIdWithActiveMembers(@Param("eventId") UUID eventId);
    List<Teams> findByLeaderUserId(UUID leaderUserId);

    @Query("""
            select distinct team
            from Teams team
            left join TeamMembers member on member.teamId = team.teamId
            where (team.leaderUserId = :userId)
               or (member.userId = :userId and member.active = true)
            """)
    List<Teams> findByUserId(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select team from Teams team where team.teamId = :teamId")
    Optional<Teams> findByIdForUpdate(@Param("teamId") UUID teamId);

    // Lấy tất cả teams trong một category (dùng cho mentor dashboard)
    List<Teams> findByCategoryId(UUID categoryId);

    // Đếm số teams trong một category
    long countByCategoryId(UUID categoryId);

    long countByEventId(UUID eventId);
}
