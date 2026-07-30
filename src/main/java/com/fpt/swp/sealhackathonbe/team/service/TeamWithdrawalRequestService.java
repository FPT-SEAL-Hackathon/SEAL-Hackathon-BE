package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamWithdrawalRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamWithdrawalRequestResponse;

import java.util.List;
import java.util.UUID;

public interface TeamWithdrawalRequestService {
    // Leader rut team ACTIVE; implementation cap nhat team, participant va submission lien quan.
    TeamWithdrawalRequestResponse requestWithdrawal(
            UUID teamId,
            CreateTeamWithdrawalRequest request,
            UUID currentUserId
    );

    // Organizer xem cac withdrawal request theo event de doi soat team da rut.
    List<TeamWithdrawalRequestResponse> getWithdrawalRequests(UUID eventId);

    // Leader/member xem cac request do chinh minh tao.
    List<TeamWithdrawalRequestResponse> getMyWithdrawalRequests(UUID currentUserId);
}
