package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.team.dto.*;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TeamController {
    // TODO: ID người dùng hiện tại đang được hard-code để test luồng API.
    // Khi tích hợp đăng nhập, lấy giá trị này từ SecurityContext/JWT thay vì giữ cố định.
    private static final UUID CURRENT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final TeamDisqualificationService teamDisqualificationService;

    // Luồng tạo team: client gửi thông tin team -> controller gắn currentUserId -> service tạo Teams và TeamMembers leader.
    @PostMapping("/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request
    ) {
        TeamResponse response = teamService.createTeam(request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    // Luồng xem team của tôi: currentUserId -> tìm TeamMembers active -> lấy Teams và danh sách thành viên.
    @GetMapping("/teams/my-team")
    public ResponseEntity<TeamResponse> getMyTeam() {
        TeamResponse response = teamService.getMyTeam(CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    // Luồng xem team theo ID: teamId -> service tìm Teams -> lấy danh sách thành viên active -> trả TeamResponse.
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(
            @PathVariable UUID teamId
    ) {
        TeamResponse response = teamService.getById(teamId);
        return ResponseEntity.ok(response);
    }

    // Luồng xin vào team: teamId + currentUserId -> service kiểm tra điều kiện -> tạo TeamJoinRequests PENDING.
    @PostMapping("/teams/{teamId}/join")
    public ResponseEntity<JoinTeamRequestResponse> requestToJoinTeam(
            @PathVariable UUID teamId
    ) {
        JoinTeamRequestResponse response = teamJoinRequestService.requestToJoinTeam(teamId, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    // Luồng leader xem request: teamId + leaderUserId -> chỉ leader được lấy danh sách request PENDING.
    @GetMapping("/teams/{teamId}/requests")
    public ResponseEntity<List<JoinTeamRequestResponse>> getPendingJoinRequests(
            @PathVariable UUID teamId
    ) {
        List<JoinTeamRequestResponse> response =
                teamJoinRequestService.getPendingJoinRequests(teamId, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    // Luồng duyệt/từ chối request: requestId + action -> nếu APPROVED thì tạo TeamMembers, nếu REJECTED thì chỉ cập nhật trạng thái.
    @PutMapping("/teams/requests/{requestId}")
    public ResponseEntity<JoinTeamRequestResponse> handleJoinRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody HandleJoinRequest request
    ) {
        JoinTeamRequestResponse response =
                teamJoinRequestService.handleJoinRequest(requestId, request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    // Luồng rời/xóa thành viên: leader có thể xóa thành viên khác, member chỉ có thể tự rời team; leader không bị xóa.
    @DeleteMapping("/teams/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID userId
    ) {
        teamService.removeMember(userId, CURRENT_USER_ID);
        return ResponseEntity.noContent().build();
    }

    // Luồng admin loại team: admin gửi lý do -> service đổi trạng thái team và lưu bản ghi Disqualifications.
    @PostMapping("/admin/teams/{teamId}/disqualify")
    public ResponseEntity<DisqualificationResponse> disqualifyTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody DisqualifyTeamRequest request
    ) {
        DisqualificationResponse response =
                teamDisqualificationService.disqualifyTeam(teamId, request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }
}
