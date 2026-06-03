package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamJoinRequestsRepository extends JpaRepository<TeamJoinRequests, Integer> {

    boolean existsByTeamIdAndUserIdAndRequestStatus(Integer teamId, Integer userId, String requestStatus);

    Optional<TeamJoinRequests> findbyRequestIdAndRequestStatus(Integer requestId, String requestStatus);

    List<TeamJoinRequests> findByteamIdAndRequestStatus(Integer teamId, String requestStatus);
}
