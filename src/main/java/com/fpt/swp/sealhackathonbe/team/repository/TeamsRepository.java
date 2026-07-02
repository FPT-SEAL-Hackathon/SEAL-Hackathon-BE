package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamsRepository extends JpaRepository<Teams, UUID> {
    // Kiểm tra trùng tên team trong cùng event trước khi tạo team mới.
    boolean existsByEventIdAndTeamName(UUID eventId, String teamName);

    List<Teams> findByEventId(UUID eventId);
    List<Teams> findByLeaderUserId(UUID leaderUserId);
}
