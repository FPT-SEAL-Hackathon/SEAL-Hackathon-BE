package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.HandleTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;

import java.util.List;
import java.util.UUID;

public interface TeamWithdrawalRequestService {
    TeamWithdrawalRequestResponse requestWithdrawal(
            UUID teamId,
            CreateTeamWithdrawalRequest request,
            UUID currentUserId
    );

    List<TeamWithdrawalRequestResponse> getPendingWithdrawalRequests(UUID eventId);

    List<TeamWithdrawalRequestResponse> getMyPendingWithdrawalRequests(UUID currentUserId);

    TeamWithdrawalRequestResponse handleWithdrawalRequest(
            UUID requestId,
            HandleTeamWithdrawalRequest request,
            UUID organizerUserId
    );
}
