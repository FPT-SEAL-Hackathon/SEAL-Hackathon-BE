package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;

import java.util.List;
import java.util.UUID;

public interface TeamWithdrawalRequestService {
    TeamWithdrawalRequestResponse requestWithdrawal(
            UUID teamId,
            CreateTeamWithdrawalRequest request,
            UUID currentUserId
    );

    List<TeamWithdrawalRequestResponse> getWithdrawalRequests(UUID eventId);

    List<TeamWithdrawalRequestResponse> getMyWithdrawalRequests(UUID currentUserId);
}
