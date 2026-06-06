package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamJoinRequestsRepository extends JpaRepository<TeamJoinRequests, UUID> {

    boolean existsByTeamIdAndUserIdAndRequestStatus(UUID teamId, UUID userId, String requestStatus);

    Optional<TeamJoinRequests> findByRequestIdAndRequestStatus(UUID requestId, String requestStatus);

    List<TeamJoinRequests> findByTeamIdAndRequestStatus(UUID teamId, String requestStatus);


}
