package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;

import java.util.List;
import java.util.UUID;

public interface TeamJoinRequestService {
    // User gửi yêu cầu tham gia team, kết quả ban đầu luôn là PENDING.
    JoinTeamRequestResponse requestToJoinTeam(UUID teamId, UUID currentUserId);

    // Leader lấy danh sách request đang chờ của team mình quản lý.
    List<JoinTeamRequestResponse> getPendingJoinRequests(UUID teamId, UUID leaderUserId);

    // Leader duyệt hoặc từ chối một request đang PENDING.
    JoinTeamRequestResponse handleJoinRequest(
            UUID requestId,
            HandleJoinRequest request,
            UUID leaderUserId
    );
}
