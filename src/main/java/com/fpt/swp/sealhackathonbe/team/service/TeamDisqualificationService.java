package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;

import java.util.UUID;

public interface TeamDisqualificationService {
    DisqualificationResponse disqualifyTeam(
            UUID teamId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    );
}
