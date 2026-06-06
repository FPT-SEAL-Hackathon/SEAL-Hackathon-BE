package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeamsRepository extends JpaRepository<Teams, UUID> {
    boolean existsByEventIdAndTeamName(UUID eventId, String teamName);
}
