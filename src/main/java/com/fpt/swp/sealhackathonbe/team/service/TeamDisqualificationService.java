package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;

import java.util.List;
import java.util.UUID;

public interface TeamDisqualificationService {
    // Admin loại team khỏi cuộc thi và lưu lý do loại vào bảng Disqualifications.
    DisqualificationResponse disqualifyTeam(
            UUID teamId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    );

    List<DisqualifiedTeamResponse> getDisqualifiedTeams();
}
