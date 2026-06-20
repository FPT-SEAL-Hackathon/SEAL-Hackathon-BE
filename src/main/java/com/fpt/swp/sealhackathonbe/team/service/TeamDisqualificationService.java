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

    // Organizer loai hang loat cac team trong event chua dat dieu kien so luong thanh vien.
    List<DisqualificationResponse> disqualifyIneligibleTeams(
            UUID eventId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    );

    // Lay team bi loai theo round va category, sap xep ban ghi loai moi nhat truoc.
    List<DisqualifiedTeamResponse> getDisqualifiedTeams(UUID roundId, UUID categoryId);
}
