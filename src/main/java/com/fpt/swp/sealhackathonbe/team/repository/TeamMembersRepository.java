package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, Integer> {
   Optional<TeamMembers> findByUserIdAndActiveTrue(Integer userid);

   Optional<TeamMembers> findByTeamIdAndUserIdAndActiveTrue(Integer teamId, Integer userId);

   boolean existsByUserIdAndActiveTrue(Integer userId);

   long countByTeamIdAndActiveTrue(Integer teamId);
}
