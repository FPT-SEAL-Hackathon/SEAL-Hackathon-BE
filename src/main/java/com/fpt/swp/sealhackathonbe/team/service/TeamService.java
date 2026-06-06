package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;

import java.util.UUID;

public interface TeamService {
    TeamResponse createTeam(CreateTeamRequest request, UUID currentUserId);

    TeamResponse getMyTeam(UUID currentUserId);

    void removeMember(UUID userId, UUID currentUserId);
}
