package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;

import java.util.List;
import java.util.UUID;

public interface TeamJoinRequestService {
    JoinTeamRequestResponse requestToJoinTeam(UUID teamId, UUID currentUserId);

    List<JoinTeamRequestResponse> getPendingJoinRequests(UUID teamId, UUID leaderUserId);

    JoinTeamRequestResponse handleJoinRequest(
            UUID requestId,
            HandleJoinRequest request,
            UUID leaderUserId
    );
}
