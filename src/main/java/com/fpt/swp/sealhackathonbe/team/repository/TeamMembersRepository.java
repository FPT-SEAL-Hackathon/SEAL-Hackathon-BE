package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, UUID> {
   Optional<TeamMembers> findByUserIdAndActiveTrue(UUID userId);

   Optional<TeamMembers> findByTeamIdAndUserIdAndActiveTrue(UUID teamId, UUID userId);

   List<TeamMembers> findByTeamIdAndActiveTrue(UUID teamId);

   boolean existsByUserIdAndActiveTrue(UUID userId);

   long countByTeamIdAndActiveTrue(UUID teamId);
}
