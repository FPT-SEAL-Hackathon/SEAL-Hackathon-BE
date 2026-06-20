package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamEligibilityReviewResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    // Tạo team mới và tự động thêm currentUserId làm leader/member đầu tiên.
    TeamResponse createTeam(CreateTeamRequest request, UUID currentUserId);

    // Lấy team active mà currentUserId đang tham gia.
    TeamResponse getMyTeam(UUID currentUserId);

    // Lấy team theo ID, kèm danh sách thành viên active của team đó.
    TeamResponse getById(UUID teamId);

    List<TeamResponse> getByEventId(UUID eventId);

    List<TeamEligibilityReviewResponse> reviewTeamsEligibility(UUID eventId);

    TeamResponse activateTeam(UUID teamId);

    // Lấy chi tiết một thành viên active trong team, bao gồm thông tin membership và hồ sơ user.
    TeamMemberDetailResponse getTeamMemberDetail(UUID teamId, UUID userId, UUID currentUserId);

    // Đánh dấu member inactive khi rời team hoặc bị leader xóa.
    void removeMember(UUID userId, UUID currentUserId);
}
